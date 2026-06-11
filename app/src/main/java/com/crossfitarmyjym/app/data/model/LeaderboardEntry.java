package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {

    @SerializedName("rank")
    private int rank;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("score")
    private double score;
    @SerializedName("formatted_score")
    private String formattedScore;
    @SerializedName("is_pr")
    private boolean pr;

    public int getRank() {
        return rank;
    }

    public String getFullName() {
        return fullName != null ? fullName : "";
    }

    public double getScore() {
        return score;
    }

    public String getFormattedScore() {
        return formattedScore != null && !formattedScore.isEmpty()
                ? formattedScore : String.valueOf(score);
    }

    public boolean isPr() {
        return pr;
    }
}
