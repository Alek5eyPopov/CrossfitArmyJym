package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель связи WOD и упражнения.
 * Соответствует таблице wod_exercises в Supabase.
 */
public class WodExercise {

    @SerializedName("id")
    private String id;

    @SerializedName("wod_id")
    private String wodId;

    @SerializedName("exercise_id")
    private String exerciseId;

    @SerializedName("rounds")
    private int rounds;

    @SerializedName("recommended_weight_kg")
    private int recommendedWeightKg;

    @SerializedName("custom_instruction")
    private String customInstruction;

    @SerializedName("created_at")
    private String createdAt;

    public WodExercise() {
    }

    public String getId() {
        return id;
    }

    public String getWodId() {
        return wodId;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public int getRounds() {
        return rounds;
    }

    public int getRecommendedWeightKg() {
        return recommendedWeightKg;
    }

    public String getCustomInstruction() {
        return customInstruction;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}