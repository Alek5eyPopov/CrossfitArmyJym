package com.crossfitarmyjym.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room-сущность для кэширования связи WOD и упражнений.
 */
@Entity(tableName = "cached_wod_exercises")
public class WodExerciseEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "wod_id")
    private String wodId;

    @ColumnInfo(name = "exercise_id")
    private String exerciseId;

    @ColumnInfo(name = "rounds")
    private int rounds;

    @ColumnInfo(name = "recommended_weight_kg")
    private int recommendedWeightKg;

    @ColumnInfo(name = "custom_instruction")
    private String customInstruction;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public WodExerciseEntity(@NonNull String id) {
        this.id = id;
        this.cachedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getWodId() { return wodId; }
    public void setWodId(String wodId) { this.wodId = wodId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public int getRounds() { return rounds; }
    public void setRounds(int rounds) { this.rounds = rounds; }

    public int getRecommendedWeightKg() { return recommendedWeightKg; }
    public void setRecommendedWeightKg(int recommendedWeightKg) { this.recommendedWeightKg = recommendedWeightKg; }

    public String getCustomInstruction() { return customInstruction; }
    public void setCustomInstruction(String customInstruction) { this.customInstruction = customInstruction; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}