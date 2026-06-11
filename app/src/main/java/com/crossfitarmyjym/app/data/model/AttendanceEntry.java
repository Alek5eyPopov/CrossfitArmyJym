package com.crossfitarmyjym.app.data.model;

public class AttendanceEntry {

    private final String userId;
    private final String fullName;
    private final String email;
    private boolean attended;

    public AttendanceEntry(String userId, String fullName, String email, boolean attended) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.attended = attended;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public Attendance toAttendance(String classId, String trainerId) {
        Attendance attendance = new Attendance();
        attendance.setClassId(classId);
        attendance.setUserId(userId);
        attendance.setAttended(attended);
        attendance.setMarkedBy(trainerId);
        return attendance;
    }
}
