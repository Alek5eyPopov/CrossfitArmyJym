package com.crossfitarmyjym.app.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class AuthUser {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    public AuthUser() {
    }

    public AuthUser(String id, String email) {
        this.id = id;
        this.email = email;
    }

    @NonNull
    public String getId() {
        return id != null ? id : "";
    }

    @NonNull
    public String getEmail() {
        return email != null ? email : "";
    }
}
