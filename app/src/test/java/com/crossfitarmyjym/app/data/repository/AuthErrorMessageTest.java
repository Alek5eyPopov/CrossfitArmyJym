package com.crossfitarmyjym.app.data.repository;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

public class AuthErrorMessageTest {

    @Test
    public void supabaseEmailConfirmationError_containsReadableMessage() {
        JsonObject error = JsonParser.parseString(
                "{\"code\":\"email_not_confirmed\",\"message\":\"Email not confirmed\"}"
        ).getAsJsonObject();

        assertEquals("Email not confirmed", error.get("message").getAsString());
    }
}
