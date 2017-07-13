package io.searchbox.client.http;

import org.apache.http.ConnectionClosedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpRetryHandlerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructorThrowsIllegalArgumentExceptionIfRetryCountIsNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("retryCount must be positive");

        new HttpRetryHandler(-1);
    }

    @Test
    public void constructorThrowsIllegalArgumentExceptionIfExceptionClassesAreEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("exceptionClasses must not be empty");

        new HttpRetryHandler(0, Collections.emptyList());
    }

    @Test
    public void retryRequestReturnsTrueIfRetryCountHasNotBeenReachedAndExceptionIsSupportedWithDefaultConstructor() {
        HttpRetryHandler retryHandler = new HttpRetryHandler(1);
        assertTrue(retryHandler.retryRequest(new UnknownHostException("message"), 0, null));
        assertTrue(retryHandler.retryRequest(new ConnectException("message"), 0, null));
        assertTrue(retryHandler.retryRequest(new ConnectionClosedException("message"), 0, null));
        assertTrue(retryHandler.retryRequest(new SSLException("message"), 0, null));
    }

    @Test
    public void retryRequestReturnsTrueIfRetryCountHasNotBeenReachedAndExceptionIsSupported() {
        HttpRetryHandler retryHandler = new HttpRetryHandler(2, Collections.singleton(ConnectionClosedException.class));
        assertTrue(retryHandler.retryRequest(new ConnectionClosedException("message"), 0, null));
    }

    @Test
    public void retryRequestReturnsFalseIfRetryCountHasNotBeenReachedAndExceptionIsNotSupported() {
        HttpRetryHandler retryHandler = new HttpRetryHandler(2, Collections.singleton(IOException.class));
        assertFalse(retryHandler.retryRequest(new RuntimeException("message"), 0, null));
    }

    @Test
    public void retryRequestReturnsFalseIfRetryCountHasBeenReachedAndExceptionIsSupported() {
        HttpRetryHandler retryHandler = new HttpRetryHandler(2, Collections.singleton(IOException.class));
        assertFalse(retryHandler.retryRequest(new ConnectionClosedException("message"), 3, null));
    }

    @Test
    public void retryRequestReturnsTrueTheCorrectNumberOfTimes() {
        HttpRetryHandler retryHandler = new HttpRetryHandler(2, Collections.singleton(ConnectionClosedException.class));

        assertTrue(retryHandler.retryRequest(new ConnectionClosedException("message"), 0, null));
        assertTrue(retryHandler.retryRequest(new ConnectionClosedException("message"), 1, null));
        assertFalse(retryHandler.retryRequest(new ConnectionClosedException("message"), 2, null));
    }
}