package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

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

    public void setId(String id) {
        this.id = id;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
