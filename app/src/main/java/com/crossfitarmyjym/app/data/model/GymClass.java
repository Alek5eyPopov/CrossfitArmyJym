package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель занятия в расписании.
 * Соответствует таблице classes в Supabase.
 */
public class GymClass {

    @SerializedName("id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("trainer_id")
    private String trainerId;

    @SerializedName("wod_id")
    private String wodId;

    @SerializedName("scheduled_start")
    private String scheduledStart;

    @SerializedName("scheduled_end")
    private String scheduledEnd;

    @SerializedName("max_capacity")
    private int maxCapacity;

    @SerializedName("current_bookings")
    private int currentBookings;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public GymClass() {
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public String getWodId() {
        return wodId;
    }

    public String getScheduledStart() {
        return scheduledStart;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public int getAvailableSlots() {
        return maxCapacity - currentBookings;
    }

    public boolean hasAvailableSlots() {
        return currentBookings < maxCapacity;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}