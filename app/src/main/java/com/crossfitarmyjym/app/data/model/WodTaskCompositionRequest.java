package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WodTaskCompositionRequest {

    @SerializedName("p_name")
    private final String name;

    @SerializedName("p_format")
    private final String format;

    @SerializedName("p_target_group_id")
    private final String targetGroupId;

    @SerializedName("p_scheduled_date")
    private final String scheduledDate;

    @SerializedName("p_time_cap_seconds")
    private final int timeCapSeconds;

    @SerializedName("p_notes")
    private final String notes;

    @SerializedName("p_tasks")
    private final List<WodTaskInput> tasks;

    public WodTaskCompositionRequest(String name, String format, String targetGroupId,
                                     String scheduledDate, int timeCapSeconds,
                                     String notes, List<WodTaskInput> tasks) {
        this.name = name;
        this.format = format;
        this.targetGroupId = targetGroupId;
        this.scheduledDate = scheduledDate;
        this.timeCapSeconds = Math.max(timeCapSeconds, 0);
        this.notes = notes;
        this.tasks = tasks;
    }
}
