package com.crossfitarmyjym.app.data.api;

import static org.junit.Assert.assertEquals;

import com.crossfitarmyjym.app.data.model.SignupRequest;

import org.junit.Test;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthApiUrlTest {

    @Test
    public void signup_resolvesFromProjectRootNotRestPath() {
        AuthApi api = new Retrofit.Builder()
                .baseUrl("https://project.supabase.co/rest/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi.class);

        String url = api.signup(
                "crossfitarmyjym://email-confirmed",
                new SignupRequest("test@example.com", "password", "Test User")
        ).request().url().toString();

        assertEquals(
                "https://project.supabase.co/auth/v1/signup"
                        + "?redirect_to=crossfitarmyjym%3A%2F%2Femail-confirmed",
                url
        );
    }
}
