package io.searchbox.core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PingTest {
    @Test
    public void testBasicUriGeneration() throws IOException {
        Ping ping = new Ping.Builder().build();

        assertEquals("GET", ping.getRestMethodName());
        assertNull(ping.getData(null));
        assertEquals("", ping.getURI());
    }
}
