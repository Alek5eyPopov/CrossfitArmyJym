package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования расписания занятий.
 */
@Entity(tableName = "cached_classes")
public class GymClassEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "trainer_id")
    private String trainerId;

    @ColumnInfo(name = "scheduled_start")
    private String scheduledStart;

    @ColumnInfo(name = "scheduled_end")
    private String scheduledEnd;

    @ColumnInfo(name = "max_capacity")
    private int maxCapacity;

    @ColumnInfo(name = "current_bookings")
    private int currentBookings;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public GymClassEntity(@NonNull String id) {
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

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public String getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(String scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(String scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public void setCurrentBookings(int currentBookings) {
        this.currentBookings = currentBookings;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}