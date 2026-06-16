package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class TrainingTask {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("rx_exercise_id")
    private String rxExerciseId;

    @SerializedName("load_type_id")
    private String loadTypeId;

    @SerializedName("rx_load_description")
    private String rxLoadDescription;

    @SerializedName("optional_exercise_id")
    private String optionalExerciseId;

    @SerializedName("optional_load_type_id")
    private String optionalLoadTypeId;

    @SerializedName("optional_load_description")
    private String optionalLoadDescription;

    @SerializedName("notes")
    private String notes;

    @SerializedName("is_active")
    private boolean active;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("rx_exercise")
    private Exercise rxExercise;

    @SerializedName("load_type")
    private LoadType loadType;

    @SerializedName("optional_exercise")
    private Exercise optionalExercise;

    @SerializedName("optional_load_type")
    private LoadType optionalLoadType;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRxExerciseId() {
        return rxExerciseId;
    }

    public String getLoadTypeId() {
        return loadTypeId;
    }

    public String getRxLoadDescription() {
        return rxLoadDescription;
    }

    public String getOptionalExerciseId() {
        return optionalExerciseId;
    }

    public String getOptionalLoadTypeId() {
        return optionalLoadTypeId;
    }

    public String getOptionalLoadDescription() {
        return optionalLoadDescription;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Exercise getRxExercise() {
        return rxExercise;
    }

    public LoadType getLoadType() {
        return loadType;
    }

    public Exercise getOptionalExercise() {
        return optionalExercise;
    }

    public LoadType getOptionalLoadType() {
        return optionalLoadType;
    }
}
