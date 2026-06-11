package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.ResultEntity;

import java.util.List;

/**
 * DAO для кэша результатов тренировок.
 */
@Dao
public interface ResultDao {

    @Query("SELECT * FROM cached_results WHERE user_id = :userId ORDER BY completed_at DESC")
    List<ResultEntity> getResultsByUser(String userId);

    @Query("SELECT * FROM cached_results WHERE wod_id = :wodId")
    List<ResultEntity> getResultsByWod(String wodId);

    @Query("SELECT * FROM cached_results WHERE id = :resultId LIMIT 1")
    ResultEntity getResultById(String resultId);

    @Query("SELECT * FROM cached_results WHERE user_id = :userId AND is_pr = 1")
    List<ResultEntity> getPersonalRecords(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ResultEntity> results);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ResultEntity result);

    @Query("DELETE FROM cached_results")
    void clearAll();
}