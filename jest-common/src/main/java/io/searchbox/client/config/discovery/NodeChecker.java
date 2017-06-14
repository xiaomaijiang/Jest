package io.searchbox.client.config.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.exception.CouldNotConnectException;
import io.searchbox.cluster.NodesInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Discovers new nodes by calling NodesInfo API on the next available server
 * and parses the <code>nodes</code> object in response to get http publish
 * address.
 */
public class NodeChecker extends AbstractScheduledService {

    private final static Logger log = LoggerFactory.getLogger(NodeChecker.class);
    private final static String HTTP_ADDRESS_KEY = "http_address";
    private final static String PUBLISH_ADDRESS_KEY = "publish_address";
    private final static Pattern INETSOCKETADDRESS_PATTERN = Pattern.compile("(?:inet\\[)?(?:(?:[^:]+)?\\/)?([^:]+):(\\d+)\\]?");

    private final NodesInfo action;

    protected JestClient client;
    protected Scheduler scheduler;
    protected String defaultScheme;
    protected Set<String> bootstrapServerList;
    protected Set<String> discoveredServerList;

    public NodeChecker(JestClient jestClient, ClientConfig clientConfig) {
        action = new NodesInfo.Builder()
                .withHttp()
                .addNode(clientConfig.getDiscoveryFilter())
                .build();
        this.client = jestClient;
        this.defaultScheme = clientConfig.getDefaultSchemeForDiscoveredNodes();
        this.scheduler = Scheduler.newFixedDelaySchedule(
                0l,
                clientConfig.getDiscoveryFrequency(),
                clientConfig.getDiscoveryFrequencyTimeUnit()
        );
        this.bootstrapServerList = ImmutableSet.copyOf(clientConfig.getServerList());
        this.discoveredServerList = new LinkedHashSet<String>();
    }

    @Override
    protected void runOneIteration() throws Exception {
        JestResult result;
        try {
            result = client.execute(action);
        } catch (CouldNotConnectException cnce) {
            // Can't connect to this node, remove it from the list
            log.error("Connect exception executing NodesInfo!", cnce);
            removeNodeAndUpdateServers(cnce.getHost());
            return;
            // do not elevate the exception since that will stop the scheduled calls.
            // throw new RuntimeException("Error executing NodesInfo!", e);
        } catch (Exception e) {
            log.error("Error executing NodesInfo!", e);
            client.setServers(bootstrapServerList);
            return;
            // do not elevate the exception since that will stop the scheduled calls.
            // throw new RuntimeException("Error executing NodesInfo!", e);
        }

        if (result.isSucceeded()) {
            LinkedHashSet<String> httpHosts = new LinkedHashSet<String>();

            JsonNode jsonMap = result.getJsonObject();
            JsonNode nodes = jsonMap.get("nodes");
            if (nodes != null) {
                for (JsonNode host : nodes) {
                    // get as a JsonNode first as some nodes in the cluster may not have an http_address
                    if (host.has(HTTP_ADDRESS_KEY)) {
                        JsonNode addressElement = host.get(HTTP_ADDRESS_KEY);
                        if (!addressElement.isNull()) {
                            String httpAddress = getHttpAddress(addressElement.asText());
                            if (httpAddress != null) {
                                httpHosts.add(httpAddress);
                            }
                        }
                    } else {
                        String httpAddress = acquirePublishAddress(host);
                        if (httpAddress != null) {
                            httpHosts.add(httpAddress);
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Discovered {} HTTP hosts: {}", httpHosts.size(), Joiner.on(',').join(httpHosts));
            }
            discoveredServerList = httpHosts;
            client.setServers(discoveredServerList);
        } else {
            log.warn("NodesInfo request resulted in error: {}", result.getErrorMessage());
            client.setServers(bootstrapServerList);
        }
    }

    private String acquirePublishAddress(JsonNode json) {
        if (json.has("http")) {
            ObjectNode http = (ObjectNode) json.get("http");
            if (http.has(PUBLISH_ADDRESS_KEY)) {
                JsonNode publishAddress = http.get(PUBLISH_ADDRESS_KEY);
                if (!publishAddress.isNull()) {
                    return getHttpAddress(publishAddress.asText());
                }
            }
        }
        return null;
    }

    protected void removeNodeAndUpdateServers(final String hostToRemove) {
        log.warn("Removing host {}", hostToRemove);
        discoveredServerList.remove(hostToRemove);
        if (log.isInfoEnabled()) {
            log.info("Discovered server pool is now: {}", Joiner.on(',').join(discoveredServerList));
        }
        if (!discoveredServerList.isEmpty()) {
          client.setServers(discoveredServerList);
        } else {
          client.setServers(bootstrapServerList);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }

    @Override
    protected ScheduledExecutorService executor() {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(serviceName())
                .build());
        // Add a listener to shutdown the executor after the service is stopped.  This ensures that the
        // JVM shutdown will not be prevented from exiting after this service has stopped or failed.
        // Technically this listener is added after start() was called so it is a little gross, but it
        // is called within doStart() so we know that the service cannot terminate or fail concurrently
        // with adding this listener so it is impossible to miss an event that we are interested in.
        addListener(new Listener() {
            @Override public void terminated(State from) {
                executor.shutdown();
            }
            @Override public void failed(State from, Throwable failure) {
                executor.shutdown();
            }}, MoreExecutors.directExecutor());
        return executor;
    }

    /**
     * Converts the Elasticsearch reported publish address in the format "inet[<hostname>:<port>]" or
     * "inet[<hostname>/<hostaddress>:<port>]" to a normalized http address in the form "http://host:port".
     */
    protected String getHttpAddress(String httpAddress) {
        Matcher resolvedMatcher = INETSOCKETADDRESS_PATTERN.matcher(httpAddress);
        if (resolvedMatcher.matches()) {
            return defaultScheme + resolvedMatcher.group(1) + ":" + resolvedMatcher.group(2);
        }

        return null;
    }

}
