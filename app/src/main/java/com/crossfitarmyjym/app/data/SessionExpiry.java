package com.crossfitarmyjym.app.data;

public final class SessionExpiry {

    private static final long REFRESH_LEEWAY_MILLIS = 60_000L;

    private SessionExpiry() {
    }

    public static long calculateExpiresAt(long nowMillis, int expiresInSeconds) {
        if (expiresInSeconds <= 0) {
            return nowMillis;
        }
        return nowMillis + expiresInSeconds * 1_000L;
    }

    public static boolean shouldRefresh(long expiresAtMillis, long nowMillis) {
        return expiresAtMillis <= 0
                || expiresAtMillis - nowMillis <= REFRESH_LEEWAY_MILLIS;
    }
}
