package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель результата тренировки.
 * Соответствует таблице results в Supabase.
 */
public class Result {

    @SerializedName("id")
    private String id;

    @SerializedName("wod_id")
    private String wodId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("score")
    private double score;

    @SerializedName("formatted_score")
    private String formattedScore;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("is_pr")
    private boolean isPr;

    @SerializedName("synced_at")
    private String syncedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("wods")
    private Wod wod;

    public Result() {
    }

    public String getId() {
        return id;
    }

    public String getWodId() {
        return wodId;
    }

    public String getUserId() {
        return userId;
    }

    public double getScore() {
        return score;
    }

    public String getFormattedScore() {
        return formattedScore;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public boolean isPr() {
        return isPr;
    }

    public String getSyncedAt() {
        return syncedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Wod getWod() {
        return wod;
    }
}
