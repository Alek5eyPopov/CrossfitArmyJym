package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.AttendanceEntity;

import java.util.List;

/**
 * DAO для кэша посещаемости.
 */
@Dao
public interface AttendanceDao {

    @Query("SELECT * FROM cached_attendance WHERE class_id = :classId")
    List<AttendanceEntity> getAttendanceByClass(String classId);

    @Query("SELECT * FROM cached_attendance WHERE user_id = :userId")
    List<AttendanceEntity> getAttendanceByUser(String userId);

    @Query("SELECT * FROM cached_attendance WHERE class_id = :classId AND user_id = :userId LIMIT 1")
    AttendanceEntity getAttendance(String classId, String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AttendanceEntity> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AttendanceEntity item);

    @Query("DELETE FROM cached_attendance WHERE class_id = :classId")
    void deleteByClass(String classId);

    @Query("DELETE FROM cached_attendance")
    void clearAll();
}