package io.searchbox.client.http;

import com.google.common.base.Preconditions;
import io.searchbox.client.JestRetryHandler;
import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

public class HttpRetryHandler implements JestRetryHandler<HttpUriRequest> {
    private static final Logger log = LoggerFactory.getLogger(HttpRetryHandler.class);

    private final int retryCount;
    private final Collection<Class<? extends Exception>> exceptionClasses;

    public HttpRetryHandler(int retryCount) {
        this(retryCount, Arrays.asList(UnknownHostException.class,
                ConnectException.class,
                ConnectionClosedException.class,
                SSLException.class));
    }

    public HttpRetryHandler(int retryCount, Collection<Class<? extends Exception>> exceptionClasses) {
        Preconditions.checkArgument(retryCount >= 0, "retryCount must be positive");
        Preconditions.checkArgument(!exceptionClasses.isEmpty(), "exceptionClasses must not be empty");

        this.retryCount = retryCount;
        this.exceptionClasses = exceptionClasses;
    }

    @Override
    public boolean retryRequest(Exception exception, int executionCount, HttpUriRequest request) {
        if (executionCount >= retryCount) {
            log.debug("Maximum number of retries ({}) for request {} reached (executed {} times)",
                    retryCount, request, executionCount, exception);
            return false;
        } else {
            for (Class<? extends Exception> exceptionClass : exceptionClasses) {
                if (exceptionClass.isInstance(exception)) {
                    log.debug("Retrying request {}", request, exception);
                    return true;
                }
            }

            log.debug("Not retrying request {} due to unsupported exception", request, exception);
            return false;
        }
    }
}
