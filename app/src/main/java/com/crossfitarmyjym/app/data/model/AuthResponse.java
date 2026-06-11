package com.crossfitarmyjym.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("user")
    private AuthUser user;

    // Signup without a session returns the user object at the top level.
    @SerializedName("id")
    private String directUserId;

    @SerializedName("email")
    private String directUserEmail;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @Nullable
    public AuthUser getUser() {
        if (user != null) {
            return user;
        }
        if (directUserId != null && !directUserId.isEmpty()) {
            return new AuthUser(directUserId, directUserEmail);
        }
        return null;
    }

    public void setUser(@Nullable AuthUser user) {
        this.user = user;
    }

    @NonNull
    public String getAccessToken() {
        return accessToken != null ? accessToken : "";
    }

    public void setAccessToken(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    @NonNull
    public String getTokenType() {
        return tokenType != null ? tokenType : "";
    }

    public void setTokenType(@NonNull String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken != null ? refreshToken : "";
    }

    public void setRefreshToken(@NonNull String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean hasSession() {
        return !getAccessToken().isEmpty() && !getRefreshToken().isEmpty();
    }

    public boolean isSuccess() {
        return getUser() != null && hasSession();
    }
}
