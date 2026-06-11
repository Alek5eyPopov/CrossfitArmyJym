package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования WOD.
 */
@Entity(tableName = "cached_wods")
public class WodEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "format")
    private String format;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "trainer_id")
    private String trainerId;

    @ColumnInfo(name = "scheduled_date")
    private String scheduledDate;

    @ColumnInfo(name = "time_cap_seconds")
    private int timeCapSeconds;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public WodEntity(@NonNull String id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public int getTimeCapSeconds() {
        return timeCapSeconds;
    }

    public void setTimeCapSeconds(int timeCapSeconds) {
        this.timeCapSeconds = timeCapSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}