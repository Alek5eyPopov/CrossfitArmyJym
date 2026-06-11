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
}
