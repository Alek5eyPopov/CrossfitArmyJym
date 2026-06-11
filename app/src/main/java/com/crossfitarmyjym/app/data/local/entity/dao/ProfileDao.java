package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.ProfileEntity;

import java.util.List;

/**
 * DAO для кэша профилей пользователей.
 */
@Dao
public interface ProfileDao {

    @Query("SELECT * FROM cached_profiles WHERE id = :profileId LIMIT 1")
    ProfileEntity getProfileById(String profileId);

    @Query("SELECT * FROM cached_profiles")
    List<ProfileEntity> getAllProfiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProfileEntity profile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProfileEntity> profiles);

    @Query("DELETE FROM cached_profiles")
    void clearAll();
}