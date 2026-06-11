package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель FCM токена для push-уведомлений.
 * Соответствует таблице fcm_tokens в Supabase.
 */
public class FcmToken {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("token")
    private String token;

    @SerializedName("device_name")
    private String deviceName;

    @SerializedName("last_used")
    private String lastUsed;

    @SerializedName("created_at")
    private String createdAt;

    public FcmToken() {
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}