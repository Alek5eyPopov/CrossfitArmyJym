package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования посещаемости.
 */
@Entity(tableName = "cached_attendance")
public class AttendanceEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "class_id")
    private String classId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "attended")
    private boolean attended;

    @ColumnInfo(name = "check_in_time")
    private String checkInTime;

    @ColumnInfo(name = "marked_by")
    private String markedBy;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public AttendanceEntity(@NonNull String id) {
        this.id = id;
        this.cachedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}