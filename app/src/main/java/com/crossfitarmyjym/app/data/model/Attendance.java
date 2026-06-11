package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель посещаемости занятия.
 * Соответствует таблице attendance в Supabase.
 */
public class Attendance {

    @SerializedName("id")
    private String id;

    @SerializedName("class_id")
    private String classId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("attended")
    private boolean attended;

    @SerializedName("check_in_time")
    private String checkInTime;

    @SerializedName("marked_by")
    private String markedBy;

    @SerializedName("notes")
    private String notes;

    public Attendance() {
    }

    public String getId() {
        return id;
    }

    public String getClassId() {
        return classId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAttended() {
        return attended;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public String getNotes() {
        return notes;
    }
}