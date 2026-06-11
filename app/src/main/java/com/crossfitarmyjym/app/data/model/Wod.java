package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @SerializedName("wod_exercises")
    private List<WodExercise> exercises;

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

    public List<WodExercise> getExercises() {
        return exercises;
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFormat(String format) { this.format = format; }
    public void setTargetGroupId(String targetGroupId) { this.targetGroupId = targetGroupId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
    public void setTimeCapSeconds(int timeCapSeconds) { this.timeCapSeconds = timeCapSeconds; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
