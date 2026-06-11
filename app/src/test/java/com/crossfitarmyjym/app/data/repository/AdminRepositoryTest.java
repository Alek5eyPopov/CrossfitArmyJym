package com.crossfitarmyjym.app.data.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Map;

public class AdminRepositoryTest {

    @Test
    public void classFields_clampsCapacityAndUsesDatabaseNames() {
        Map<String, Object> fields = AdminRepository.classFields(
                "group-1", "trainer-1", "2026-06-12T18:00:00",
                "2026-06-12T19:00:00", 0, "Main Box", "scheduled"
        );

        assertEquals("group-1", fields.get("group_id"));
        assertEquals("trainer-1", fields.get("trainer_id"));
        assertEquals(1, fields.get("max_capacity"));
        assertEquals("scheduled", fields.get("status"));
    }

    @Test
    public void wodFields_onlyContainsEditableMetadata() {
        Map<String, Object> fields = AdminRepository.wodFields(
                "Murph", "2026-06-12", "Bring a vest"
        );

        assertEquals(3, fields.size());
        assertEquals("Murph", fields.get("name"));
        assertEquals("2026-06-12", fields.get("scheduled_date"));
        assertEquals("Bring a vest", fields.get("notes"));
    }
}
