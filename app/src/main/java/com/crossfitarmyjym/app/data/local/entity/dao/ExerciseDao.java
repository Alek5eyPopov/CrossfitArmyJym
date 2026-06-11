package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.ExerciseEntity;

import java.util.List;

/**
 * DAO для работы с кэшем упражнений.
 */
@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM cached_exercises ORDER BY name ASC")
    List<ExerciseEntity> getAllExercises();

    @Query("SELECT * FROM cached_exercises WHERE id = :exerciseId LIMIT 1")
    ExerciseEntity getExerciseById(String exerciseId);

    @Query("SELECT * FROM cached_exercises WHERE category = :category ORDER BY name ASC")
    List<ExerciseEntity> getExercisesByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExerciseEntity> exercises);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExerciseEntity exercise);

    @Query("DELETE FROM cached_exercises")
    void clearAll();

    @Query("DELETE FROM cached_exercises WHERE cached_at < :timestamp")
    void clearOldEntries(long timestamp);
}