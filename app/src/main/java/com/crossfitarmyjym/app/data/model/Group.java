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
}