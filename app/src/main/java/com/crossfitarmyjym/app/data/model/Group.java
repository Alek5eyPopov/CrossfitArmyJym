package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель тренировочной группы.
 * Соответствует таблице groups в Supabase.
 */
public class Group {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("trainer_id")
    private String trainerId;

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("created_at")
    private String createdAt;

    public Group() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public String getSchedule() {
        return schedule;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
