package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WodExerciseInput {

    @SerializedName("exercise_id")
    private final String exerciseId;

    @SerializedName("rounds")
    private final int rounds;

    @SerializedName("recommended_weight_kg")
    private final int recommendedWeightKg;

    @SerializedName("custom_instruction")
    private final String customInstruction;

    public WodExerciseInput(String exerciseId, int rounds, int recommendedWeightKg,
                            String customInstruction) {
        this.exerciseId = exerciseId;
        this.rounds = Math.max(rounds, 1);
        this.recommendedWeightKg = Math.max(recommendedWeightKg, 0);
        this.customInstruction = customInstruction;
    }

    public String getExerciseId() {
        return exerciseId;
    }
}
