package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.WodExerciseEntity;

import java.util.List;

/**
 * DAO для кэша связи WOD и упражнений.
 */
@Dao
public interface WodExerciseDao {

    @Query("SELECT * FROM cached_wod_exercises WHERE wod_id = :wodId")
    List<WodExerciseEntity> getExercisesByWod(String wodId);

    @Query("SELECT * FROM cached_wod_exercises WHERE id = :id LIMIT 1")
    WodExerciseEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WodExerciseEntity> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WodExerciseEntity item);

    @Query("DELETE FROM cached_wod_exercises WHERE wod_id = :wodId")
    void deleteByWod(String wodId);

    @Query("DELETE FROM cached_wod_exercises")
    void clearAll();
}