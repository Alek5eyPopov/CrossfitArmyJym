package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель записи клиента на занятие.
 * Соответствует таблице bookings в Supabase.
 */
public class Booking {

    @SerializedName("id")
    private String id;

    @SerializedName("class_id")
    private String classId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("booked_at")
    private String bookedAt;

    @SerializedName("status")
    private String status;

    @SerializedName("cancelled_at")
    private String cancelledAt;

    @SerializedName("classes")
    private GymClass gymClass;

    public Booking() {
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getClassId() {
        return classId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBookedAt() {
        return bookedAt;
    }

    public String getStatus() {
        return status;
    }

    public boolean isConfirmed() {
        return "confirmed".equals(status);
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public GymClass getGymClass() {
        return gymClass;
    }

    // --- Setters ---

    public void setId(String id) {
        this.id = id;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBookedAt(String bookedAt) {
        this.bookedAt = bookedAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setGymClass(GymClass gymClass) {
        this.gymClass = gymClass;
    }
}
