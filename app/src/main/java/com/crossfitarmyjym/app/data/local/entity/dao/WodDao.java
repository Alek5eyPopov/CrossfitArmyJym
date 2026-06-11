package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.WodEntity;

import java.util.List;

/**
 * DAO для работы с кэшем WOD.
 */
@Dao
public interface WodDao {

    @Query("SELECT * FROM cached_wods ORDER BY scheduled_date DESC")
    List<WodEntity> getAllWods();

    @Query("SELECT * FROM cached_wods WHERE id = :wodId LIMIT 1")
    WodEntity getWodById(String wodId);

    @Query("SELECT * FROM cached_wods WHERE scheduled_date = :date LIMIT 1")
    WodEntity getWodByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WodEntity> wods);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WodEntity wod);

    @Query("DELETE FROM cached_wods")
    void clearAll();

    @Query("DELETE FROM cached_wods WHERE cached_at < :timestamp")
    void clearOldEntries(long timestamp);
}