package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class PersonalRecordRequest {

    @SerializedName("p_exercise_id")
    private final String exerciseId;

    @SerializedName("p_result_value")
    private final Double resultValue;

    @SerializedName("p_result_text")
    private final String resultText;

    @SerializedName("p_unit")
    private final String unit;

    @SerializedName("p_achieved_at")
    private final String achievedAt;

    @SerializedName("p_notes")
    private final String notes;

    public PersonalRecordRequest(String exerciseId, Double resultValue, String resultText,
                                 String unit, String achievedAt, String notes) {
        this.exerciseId = exerciseId;
        this.resultValue = resultValue;
        this.resultText = resultText;
        this.unit = unit;
        this.achievedAt = achievedAt;
        this.notes = notes;
    }
}
