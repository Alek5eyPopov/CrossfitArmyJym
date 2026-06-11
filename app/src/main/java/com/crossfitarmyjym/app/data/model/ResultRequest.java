package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class ResultRequest {

    @SerializedName("p_wod_id")
    private final String wodId;
    @SerializedName("p_score")
    private Double score;
    @SerializedName("p_formatted_score")
    private String formattedScore;

    private ResultRequest(String wodId) {
        this.wodId = wodId;
    }

    public static ResultRequest submission(String wodId, double score, String formattedScore) {
        ResultRequest request = new ResultRequest(wodId);
        request.score = score;
        request.formattedScore = formattedScore;
        return request;
    }

    public static ResultRequest leaderboard(String wodId) {
        return new ResultRequest(wodId);
    }
}
