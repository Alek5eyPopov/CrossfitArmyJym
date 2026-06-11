package com.crossfitarmyjym.app.data.local.entity.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.crossfitarmyjym.app.data.local.entity.BookingEntity;

import java.util.List;

/**
 * DAO для работы с кэшем записей на занятия.
 */
@Dao
public interface BookingDao {

    @Query("SELECT * FROM cached_bookings WHERE user_id = :userId ORDER BY booked_at DESC")
    List<BookingEntity> getBookingsByUser(String userId);

    @Query("SELECT * FROM cached_bookings WHERE class_id = :classId")
    List<BookingEntity> getBookingsByClass(String classId);

    @Query("SELECT * FROM cached_bookings WHERE id = :bookingId LIMIT 1")
    BookingEntity getBookingById(String bookingId);

    @Query("SELECT * FROM cached_bookings WHERE user_id = :userId AND class_id = :classId LIMIT 1")
    BookingEntity getBookingByUserAndClass(String userId, String classId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookingEntity> bookings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookingEntity booking);

    @Query("DELETE FROM cached_bookings WHERE id = :bookingId")
    void deleteById(String bookingId);

    @Query("DELETE FROM cached_bookings")
    void clearAll();

    @Query("DELETE FROM cached_bookings WHERE cached_at < :timestamp")
    void clearOldEntries(long timestamp);
}