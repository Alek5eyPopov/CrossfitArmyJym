package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.GroupEntity;

import java.util.List;

/**
 * DAO для кэша тренировочных групп.
 */
@Dao
public interface GroupDao {

    @Query("SELECT * FROM cached_groups WHERE id = :groupId LIMIT 1")
    GroupEntity getGroupById(String groupId);

    @Query("SELECT * FROM cached_groups WHERE is_active = 1")
    List<GroupEntity> getActiveGroups();

    @Query("SELECT * FROM cached_groups WHERE trainer_id = :trainerId")
    List<GroupEntity> getGroupsByTrainer(String trainerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GroupEntity> groups);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroupEntity group);

    @Query("DELETE FROM cached_groups")
    void clearAll();
}