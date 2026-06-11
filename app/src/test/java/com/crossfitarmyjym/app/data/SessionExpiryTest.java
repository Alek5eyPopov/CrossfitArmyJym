package com.crossfitarmyjym.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SessionExpiryTest {

    @Test
    public void calculateExpiresAt_convertsSecondsToMillis() {
        assertEquals(3_601_000L, SessionExpiry.calculateExpiresAt(1_000L, 3_600));
    }

    @Test
    public void shouldRefresh_usesOneMinuteLeeway() {
        assertTrue(SessionExpiry.shouldRefresh(60_000L, 0L));
        assertFalse(SessionExpiry.shouldRefresh(60_001L, 0L));
    }

    @Test
    public void shouldRefresh_unknownExpiryRequiresRefresh() {
        assertTrue(SessionExpiry.shouldRefresh(0L, 10_000L));
    }
}
