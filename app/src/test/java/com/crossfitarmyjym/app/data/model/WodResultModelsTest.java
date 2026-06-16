package com.crossfitarmyjym.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Collections;

public class WodResultModelsTest {

    private final Gson gson = new Gson();

    @Test
    public void compositionRequest_usesRpcParameterNames() {
        WodExerciseInput exercise = new WodExerciseInput("exercise-1", 3, 40, "scale");
        WodCompositionRequest request = new WodCompositionRequest(
                "Fran", "for_time", "group-1", "2026-06-11",
                600, "notes", Collections.singletonList(exercise)
        );

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();

        assertEquals("Fran", json.get("p_name").getAsString());
        assertEquals("group-1", json.get("p_target_group_id").getAsString());
        assertEquals("exercise-1", json.getAsJsonArray("p_exercises")
                .get(0).getAsJsonObject().get("exercise_id").getAsString());
    }

    @Test
    public void taskCompositionRequest_usesRpcParameterNames() {
        WodTaskInput task = WodTaskInput.direct(
                2,
                "Deadlift",
                "exercise-1",
                "load-type-1",
                "Three sets of four reps. Weight 100kg.",
                "optional-exercise-1",
                "optional-load-type-1",
                "Three sets of four reps. Weight 60kg.",
                "Scale as needed"
        );
        WodTaskCompositionRequest request = new WodTaskCompositionRequest(
                "Strength day", "emom", "group-1", "2026-06-16",
                900, "notes", Collections.singletonList(task)
        );

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();
        JsonObject taskJson = json.getAsJsonArray("p_tasks").get(0).getAsJsonObject();

        assertEquals("Strength day", json.get("p_name").getAsString());
        assertEquals("group-1", json.get("p_target_group_id").getAsString());
        assertEquals("exercise-1", taskJson.get("rx_exercise_id").getAsString());
        assertEquals("load-type-1", taskJson.get("load_type_id").getAsString());
        assertEquals(2, taskJson.get("position").getAsInt());
    }

    @Test
    public void resultRequests_includeOnlyRequiredRpcParameters() {
        JsonObject submission = JsonParser.parseString(
                gson.toJson(ResultRequest.submission("wod-1", 125.5, "2:05"))
        ).getAsJsonObject();
        JsonObject leaderboard = JsonParser.parseString(
                gson.toJson(ResultRequest.leaderboard("wod-1"))
        ).getAsJsonObject();

        assertEquals(125.5, submission.get("p_score").getAsDouble(), 0.0);
        assertEquals("2:05", submission.get("p_formatted_score").getAsString());
        assertEquals("wod-1", leaderboard.get("p_wod_id").getAsString());
        assertFalse(leaderboard.has("p_score"));
        assertFalse(leaderboard.has("p_formatted_score"));
    }

    @Test
    public void personalRecordRequest_usesRpcParameterNames() {
        PersonalRecordRequest request = new PersonalRecordRequest(
                "exercise-1", 120.5, "120.5 kg", "kg",
                "2026-06-16", "felt good"
        );

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();

        assertEquals("exercise-1", json.get("p_exercise_id").getAsString());
        assertEquals(120.5, json.get("p_result_value").getAsDouble(), 0.0);
        assertEquals("120.5 kg", json.get("p_result_text").getAsString());
        assertEquals("2026-06-16", json.get("p_achieved_at").getAsString());
    }
}
