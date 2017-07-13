package io.searchbox.client;

public class NoopRetryHandler<T> implements JestRetryHandler<T> {
    @Override
    public boolean retryRequest(Exception exception, int executionCount, T request) {
        return false;
    }
}
