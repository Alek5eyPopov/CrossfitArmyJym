package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель упражнения.
 * Соответствует таблице exercises в Supabase.
 */
public class Exercise {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("unit_type")
    private String unitType;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_at")
    private String createdAt;

    public Exercise() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getUnitType() {
        return unitType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
