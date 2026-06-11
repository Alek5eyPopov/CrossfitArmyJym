package com.crossfitarmyjym.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

public class AttendanceEntryTest {

    @Test
    public void toAttendance_buildsPostgrestPayload() {
        AttendanceEntry entry = new AttendanceEntry(
                "user-1", "Athlete", "athlete@example.com", true);

        Attendance attendance = entry.toAttendance("class-1", "trainer-1");
        JsonObject json = JsonParser.parseString(new Gson().toJson(attendance)).getAsJsonObject();

        assertEquals("class-1", json.get("class_id").getAsString());
        assertEquals("user-1", json.get("user_id").getAsString());
        assertEquals("trainer-1", json.get("marked_by").getAsString());
        assertTrue(json.get("attended").getAsBoolean());
    }

    @Test
    public void attendanceFlag_canBeChangedBeforeSave() {
        AttendanceEntry entry = new AttendanceEntry(
                "user-1", "Athlete", "athlete@example.com", false);
        entry.setAttended(true);

        assertTrue(entry.toAttendance("class-1", "trainer-1").isAttended());
    }
}
