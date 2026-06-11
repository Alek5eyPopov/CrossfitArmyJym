package com.crossfitarmyjym.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Модель пользователя (профиль).
 * Соответствует таблице profiles в Supabase.
 */
public class User {

    @SerializedName("id")
    private UUID id;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("role")
    private String role;

    @SerializedName("group_id")
    private UUID groupId;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Пустой конструктор для Gson
    public User() {
    }

    // Конструктор с основными полями
    public User(String email, String fullName, String role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
    }

    // Геттеры и сеттеры

    @Nullable
    public UUID getId() {
        return id;
    }

    public void setId(@Nullable UUID id) {
        this.id = id;
    }

    @NonNull
    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getFullName() {
        return fullName != null ? fullName : "";
    }

    public void setFullName(@NonNull String fullName) {
        this.fullName = fullName;
    }

    @NonNull
    public String getRole() {
        return role != null ? role : "";
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    @Nullable
    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable UUID groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@Nullable String createdAt) {
        this.createdAt = createdAt;
    }

    @Nullable
    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@Nullable String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Проверка, является ли пользователь атлетом.
     * @return true если роль athlete
     */
    public boolean isAthlete() {
        return "athlete".equals(role);
    }

    /**
     * Проверка, является ли пользователь тренером.
     * @return true если роль trainer
     */
    public boolean isTrainer() {
        return "trainer".equals(role);
    }

    /**
     * Проверка, является ли пользователь администратором.
     * @return true если роль admin
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    @Override
    @NonNull
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}