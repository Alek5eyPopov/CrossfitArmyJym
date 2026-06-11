package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования записей клиента на занятия.
 */
@Entity(tableName = "cached_bookings")
public class BookingEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "class_id")
    private String classId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "booked_at")
    private String bookedAt;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public BookingEntity(@NonNull String id) {
        this.id = id;
        this.cachedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(String bookedAt) {
        this.bookedAt = bookedAt;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}