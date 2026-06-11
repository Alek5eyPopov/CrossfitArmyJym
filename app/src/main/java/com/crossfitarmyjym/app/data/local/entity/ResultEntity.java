package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования результатов тренировок.
 */
@Entity(tableName = "cached_results")
public class ResultEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "wod_id")
    private String wodId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "score")
    private double score;

    @ColumnInfo(name = "formatted_score")
    private String formattedScore;

    @ColumnInfo(name = "is_pr")
    private boolean isPr;

    @ColumnInfo(name = "completed_at")
    private String completedAt;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public ResultEntity(@NonNull String id) {
        this.id = id;
        this.cachedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getWodId() { return wodId; }
    public void setWodId(String wodId) { this.wodId = wodId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getFormattedScore() { return formattedScore; }
    public void setFormattedScore(String formattedScore) { this.formattedScore = formattedScore; }

    public boolean isPr() { return isPr; }
    public void setPr(boolean pr) { isPr = pr; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}