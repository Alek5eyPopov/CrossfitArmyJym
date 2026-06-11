package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.GymClassEntity;

import java.util.List;

/**
 * DAO для работы с кэшем расписания занятий.
 */
@Dao
public interface GymClassDao {

    @Query("SELECT * FROM cached_classes ORDER BY scheduled_start ASC")
    List<GymClassEntity> getAllClasses();

    @Query("SELECT * FROM cached_classes WHERE id = :classId LIMIT 1")
    GymClassEntity getClassById(String classId);

    @Query("SELECT * FROM cached_classes WHERE scheduled_start >= :date ORDER BY scheduled_start ASC")
    List<GymClassEntity> getClassesFromDate(String date);

    @Query("SELECT * FROM cached_classes WHERE trainer_id = :trainerId ORDER BY scheduled_start ASC")
    List<GymClassEntity> getClassesByTrainer(String trainerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GymClassEntity> classes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GymClassEntity gymClass);

    @Query("DELETE FROM cached_classes")
    void clearAll();

    @Query("DELETE FROM cached_classes WHERE cached_at < :timestamp")
    void clearOldEntries(long timestamp);
}