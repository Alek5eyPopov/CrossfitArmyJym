package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WodTaskInput {

    @SerializedName("source_task_id")
    private final String sourceTaskId;

    @SerializedName("position")
    private final int position;

    @SerializedName("title")
    private final String title;

    @SerializedName("rx_exercise_id")
    private final String rxExerciseId;

    @SerializedName("load_type_id")
    private final String loadTypeId;

    @SerializedName("load_type_code")
    private final String loadTypeCode;

    @SerializedName("rx_load_description")
    private final String rxLoadDescription;

    @SerializedName("optional_exercise_id")
    private final String optionalExerciseId;

    @SerializedName("optional_load_type_id")
    private final String optionalLoadTypeId;

    @SerializedName("optional_load_type_code")
    private final String optionalLoadTypeCode;

    @SerializedName("optional_load_description")
    private final String optionalLoadDescription;

    @SerializedName("notes")
    private final String notes;

    public WodTaskInput(String sourceTaskId, int position, String title,
                        String rxExerciseId, String loadTypeId, String loadTypeCode,
                        String rxLoadDescription, String optionalExerciseId,
                        String optionalLoadTypeId, String optionalLoadTypeCode,
                        String optionalLoadDescription, String notes) {
        this.sourceTaskId = sourceTaskId;
        this.position = Math.max(position, 1);
        this.title = title;
        this.rxExerciseId = rxExerciseId;
        this.loadTypeId = loadTypeId;
        this.loadTypeCode = loadTypeCode;
        this.rxLoadDescription = rxLoadDescription;
        this.optionalExerciseId = optionalExerciseId;
        this.optionalLoadTypeId = optionalLoadTypeId;
        this.optionalLoadTypeCode = optionalLoadTypeCode;
        this.optionalLoadDescription = optionalLoadDescription;
        this.notes = notes;
    }

    public static WodTaskInput direct(int position, String title, String rxExerciseId,
                                      String loadTypeId, String rxLoadDescription,
                                      String optionalExerciseId,
                                      String optionalLoadTypeId,
                                      String optionalLoadDescription,
                                      String notes) {
        return new WodTaskInput(null, position, title, rxExerciseId, loadTypeId,
                null, rxLoadDescription, optionalExerciseId, optionalLoadTypeId,
                null, optionalLoadDescription, notes);
    }

    public static WodTaskInput fromTemplate(int position, String sourceTaskId) {
        return new WodTaskInput(sourceTaskId, position, null, null, null,
                null, null, null, null, null, null, null);
    }
}
