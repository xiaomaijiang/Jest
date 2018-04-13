package io.searchbox.client.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.action.Action;
import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.JestRetryHandler;
import io.searchbox.client.config.exception.CouldNotConnectException;
import io.searchbox.client.http.apache.HttpDeleteWithEntity;
import io.searchbox.client.http.apache.HttpGetWithEntity;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map.Entry;
import java.util.concurrent.Future;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class JestHttpClient extends AbstractJestClient {

    private final static Logger log = LoggerFactory.getLogger(JestHttpClient.class);

    protected ContentType requestContentType = ContentType.APPLICATION_JSON.withCharset("utf-8");

    private CloseableHttpClient httpClient;
    private CloseableHttpAsyncClient asyncClient;

    private HttpClientContext httpClientContextTemplate;
    private JestRetryHandler<HttpUriRequest> retryHandler;

    /**
     * @throws IOException              in case of a problem or the connection was aborted during request,
     *                                  or in case of a problem while reading the response stream
     * @throws CouldNotConnectException if an {@link HttpHostConnectException} is encountered
     */
    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        return execute(clientRequest, null);
    }

    public <T extends JestResult> T execute(Action<T> clientRequest, RequestConfig requestConfig) throws IOException {
        final JestRetryHandler<HttpUriRequest> retryHandler = getRetryHandler();

        int executionCount = 0;
        CloseableHttpResponse response = null;
        try {
            HttpUriRequest request;
            do {
                request = prepareRequest(clientRequest, requestConfig);
                try {
                    response = executeRequest(request);
                } catch (HttpHostConnectException ex) {
                    if (!retryHandler.retryRequest(ex, executionCount++, request)) {
                        throw new CouldNotConnectException(ex.getHost().toURI(), ex);
                    }
                } catch (Exception ex) {
                    if (!retryHandler.retryRequest(ex, executionCount++, request)) {
                        throw ex;
                    }
                }
            } while (response == null);
            return deserializeResponse(response, request, clientRequest);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    log.error("Exception occurred while closing response stream.", ex);
                }
            }
        }
    }

    @Override
    public <T extends JestResult> void executeAsync(final Action<T> clientRequest, final JestResultHandler<? super T> resultHandler) throws IOException {
        executeAsync(clientRequest, resultHandler, null);
    }

    public <T extends JestResult> void executeAsync(final Action<T> clientRequest, final JestResultHandler<? super T> resultHandler, final RequestConfig requestConfig) throws IOException {
        synchronized (this) {
            if (!asyncClient.isRunning()) {
                asyncClient.start();
            }
        }

        HttpUriRequest request = prepareRequest(clientRequest, requestConfig);
        executeAsyncRequest(clientRequest, resultHandler, request);
    }

    @Override
    public void shutdownClient() {
        try {
            close();
        } catch (IOException e) {
            log.error("Exception occurred while shutting down the sync client.", e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        asyncClient.close();
        httpClient.close();
    }

    protected <T extends JestResult> HttpUriRequest prepareRequest(final Action<T> clientRequest, final RequestConfig requestConfig) throws IOException {
        String elasticSearchRestUrl = getRequestURL(getNextServer(), clientRequest.getURI());
        HttpUriRequest request = constructHttpMethod(clientRequest.getRestMethodName(), elasticSearchRestUrl, clientRequest.getData(objectMapper), requestConfig);

        log.debug("Request method={} url={}", clientRequest.getRestMethodName(), elasticSearchRestUrl);

        // add headers added to action
        for (Entry<String, Object> header : clientRequest.getHeaders().entrySet()) {
            request.addHeader(header.getKey(), header.getValue().toString());
        }

        return request;
    }

    protected CloseableHttpResponse executeRequest(HttpUriRequest request) throws IOException {
        if (httpClientContextTemplate != null) {
            return httpClient.execute(request, createContextInstance());
        }

        return httpClient.execute(request);
    }

    protected <T extends JestResult> Future<HttpResponse> executeAsyncRequest(Action<T> clientRequest, JestResultHandler<? super T> resultHandler, HttpUriRequest request) {
        if (httpClientContextTemplate != null) {
            return asyncClient.execute(request, createContextInstance(), new DefaultCallback<T>(clientRequest, request, resultHandler));
        }

        return asyncClient.execute(request, new DefaultCallback<T>(clientRequest, request, resultHandler));
    }

    protected HttpClientContext createContextInstance() {
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(httpClientContextTemplate.getCredentialsProvider());
        context.setAuthCache(httpClientContextTemplate.getAuthCache());

        return context;
    }

    protected HttpUriRequest constructHttpMethod(String methodName, String url, String payload, RequestConfig requestConfig) {
        HttpUriRequest httpUriRequest = null;

        if (methodName.equalsIgnoreCase("POST")) {
            httpUriRequest = new HttpPost(url);
            log.debug("POST method created based on client request");
        } else if (methodName.equalsIgnoreCase("PUT")) {
            httpUriRequest = new HttpPut(url);
            log.debug("PUT method created based on client request");
        } else if (methodName.equalsIgnoreCase("DELETE")) {
            httpUriRequest = new HttpDeleteWithEntity(url);
            log.debug("DELETE method created based on client request");
        } else if (methodName.equalsIgnoreCase("GET")) {
            httpUriRequest = new HttpGetWithEntity(url);
            log.debug("GET method created based on client request");
        } else if (methodName.equalsIgnoreCase("HEAD")) {
            httpUriRequest = new HttpHead(url);
            log.debug("HEAD method created based on client request");
        }

        if (httpUriRequest instanceof HttpRequestBase && requestConfig != null) {
            ((HttpRequestBase) httpUriRequest).setConfig(requestConfig);
        }

        if (httpUriRequest != null && httpUriRequest instanceof HttpEntityEnclosingRequest && payload != null) {
            EntityBuilder entityBuilder = EntityBuilder.create()
                    .setText(payload)
                    .setContentType(requestContentType);

            if (isRequestCompressionEnabled()) {
                entityBuilder.gzipCompress();
            }

            ((HttpEntityEnclosingRequest) httpUriRequest).setEntity(entityBuilder.build());
        }

        return httpUriRequest;
    }

    private <T extends JestResult> T deserializeResponse(HttpResponse response, final HttpRequest httpRequest, Action<T> clientRequest) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        try {
            return clientRequest.createNewElasticSearchResult(
                    response.getEntity() == null ? null : EntityUtils.toString(response.getEntity()),
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase(),
                    objectMapper
            );
        } catch (JsonParseException e) {
            for (Header header : response.getHeaders("Content-Type")) {
                final String mimeType = header.getValue();
                if (!mimeType.startsWith("application/json")) {
                    // probably a proxy that responded in text/html
                    final String message = "Request " + httpRequest.toString() + " yielded " + mimeType
                            + ", should be json: " + statusLine.toString();
                    throw new IOException(message, e);
                }
            }
            throw e;
        }
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpAsyncClient getAsyncClient() {
        return asyncClient;
    }

    public void setAsyncClient(CloseableHttpAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HttpClientContext getHttpClientContextTemplate() {
        return httpClientContextTemplate;
    }

    public void setHttpClientContextTemplate(HttpClientContext httpClientContext) {
        this.httpClientContextTemplate = httpClientContext;
    }

    public JestRetryHandler<HttpUriRequest> getRetryHandler() {
        return retryHandler;
    }

    public JestHttpClient setRetryHandler(JestRetryHandler<HttpUriRequest> retryHandler) {
        this.retryHandler = retryHandler;
        return this;
    }

    protected class DefaultCallback<T extends JestResult> implements FutureCallback<HttpResponse> {
        private final Action<T> clientRequest;
        private final HttpRequest request;
        private final JestResultHandler<? super T> resultHandler;

        public DefaultCallback(Action<T> clientRequest, final HttpRequest request, JestResultHandler<? super T> resultHandler) {
            this.clientRequest = clientRequest;
            this.request = request;
            this.resultHandler = resultHandler;
        }

        @Override
        public void completed(final HttpResponse response) {
            T jestResult = null;
            try {
                jestResult = deserializeResponse(response, request, clientRequest);
            } catch (Exception e) {
                failed(e);
            } catch (Throwable t) {
                failed(new Exception("Problem during request processing", t));
            }
            if (jestResult != null) resultHandler.completed(jestResult);
        }

        @Override
        public void failed(final Exception ex) {
            log.error("Exception occurred during async execution.", ex);
            if (ex instanceof HttpHostConnectException) {
                String host = ((HttpHostConnectException) ex).getHost().toURI();
                resultHandler.failed(new CouldNotConnectException(host, ex));
                return;
            }
            resultHandler.failed(ex);
        }

        @Override
        public void cancelled() {
            log.warn("Async execution was cancelled; this is not expected to occur under normal operation.");
        }
    }

}
