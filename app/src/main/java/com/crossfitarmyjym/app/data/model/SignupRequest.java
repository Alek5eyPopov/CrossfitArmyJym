package com.crossfitarmyjym.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на регистрацию нового пользователя.
 */
public class SignupRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("data")
    @Nullable
    private UserData data;

    public SignupRequest() {
    }

    public SignupRequest(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
    }

    public SignupRequest(@NonNull String email, @NonNull String password, @Nullable String fullName) {
        this.email = email;
        this.password = password;
        if (fullName != null) {
            this.data = new UserData(fullName);
        }
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @Nullable
    public UserData getData() {
        return data;
    }

    public void setData(@Nullable UserData data) {
        this.data = data;
    }

    /**
     * Вложенный класс для дополнительных данных пользователя.
     */
    public static class UserData {
        @SerializedName("full_name")
        private String fullName;

        public UserData(String fullName) {
            this.fullName = fullName;
        }

        @NonNull
        public String getFullName() {
            return fullName;
        }

        public void setFullName(@NonNull String fullName) {
            this.fullName = fullName;
        }
    }
}