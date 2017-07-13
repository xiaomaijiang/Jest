package io.searchbox.client;

/**
 * A handler for determining if a request should be retried after a
 * recoverable exception during execution.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 */
public interface JestRetryHandler<T> {

    /**
     * Determines if a method should be retried after an exception
     * occurs during execution.
     *
     * @param exception      the exception that occurred
     * @param executionCount the number of times this method has been
     *                       unsuccessfully executed
     * @param request        the original failed request
     * @return {@code true} if the request should be retried, {@code false}
     * otherwise
     */
    boolean retryRequest(Exception exception, int executionCount, T request);
}
