package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель WOD (тренировки дня).
 * Соответствует таблице wods в Supabase.
 */
public class Wod {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("format")
    private String format;

    @SerializedName("target_group_id")
    private String targetGroupId;

    @SerializedName("trainer_id")
    private String trainerId;

    @SerializedName("scheduled_date")
    private String scheduledDate;

    @SerializedName("time_cap_seconds")
    private int timeCapSeconds;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    public Wod() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getTargetGroupId() {
        return targetGroupId;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public int getTimeCapSeconds() {
        return timeCapSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}