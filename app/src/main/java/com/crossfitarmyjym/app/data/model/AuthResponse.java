package com.crossfitarmyjym.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Ответ сервера при аутентификации.
 * Содержит данные пользователя и токены.
 */
public class AuthResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("provider_token")
    @Nullable
    private String providerToken;

    @SerializedName("provider_refresh_token")
    @Nullable
    private String providerRefreshToken;

    public AuthResponse() {
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public void setUser(@Nullable User user) {
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

    @Nullable
    public String getProviderToken() {
        return providerToken;
    }

    public void setProviderToken(@Nullable String providerToken) {
        this.providerToken = providerToken;
    }

    @Nullable
    public String getProviderRefreshToken() {
        return providerRefreshToken;
    }

    public void setProviderRefreshToken(@Nullable String providerRefreshToken) {
        this.providerRefreshToken = providerRefreshToken;
    }

    /**
     * Проверка успешности ответа.
     * @return true если есть пользователь и токен
     */
    public boolean isSuccess() {
        return user != null && accessToken != null && !accessToken.isEmpty();
    }

    @Override
    @NonNull
    public String toString() {
        return "AuthResponse{" +
                "user=" + user +
                ", accessToken='***'" +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}