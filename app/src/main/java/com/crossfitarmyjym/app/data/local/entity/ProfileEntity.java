package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования профилей пользователей.
 */
@Entity(tableName = "cached_profiles")
public class ProfileEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "role")
    private String role;

    @ColumnInfo(name = "group_id")
    private String groupId;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public ProfileEntity(@NonNull String id) {
        this.id = id;
        this.cachedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}