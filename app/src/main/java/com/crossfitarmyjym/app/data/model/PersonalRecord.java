package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class PersonalRecord {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("exercise_id")
    private String exerciseId;

    @SerializedName("exercise_name")
    private String exerciseName;

    @SerializedName("result_value")
    private Double resultValue;

    @SerializedName("result_text")
    private String resultText;

    @SerializedName("unit")
    private String unit;

    @SerializedName("achieved_at")
    private String achievedAt;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("exercises")
    private Exercise exercise;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public Double getResultValue() {
        return resultValue;
    }

    public String getResultText() {
        return resultText;
    }

    public String getUnit() {
        return unit;
    }

    public String getAchievedAt() {
        return achievedAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Exercise getExercise() {
        return exercise;
    }
}
