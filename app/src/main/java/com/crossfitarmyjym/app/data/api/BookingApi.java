package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Booking;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * API интерфейс для работы с записями на занятия.
 * Использует Supabase PostgREST API (таблица bookings).
 */
public interface BookingApi {

    @GET("bookings")
    Call<List<Booking>> getBookingsByUser(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId,
            @Query("order") String order
    );

    @POST("bookings")
    Call<Booking> createBooking(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Header("Prefer") String prefer,
            @Body Booking booking
    );

    @PATCH("bookings")
    Call<Void> cancelBooking(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("id") String bookingId,
            @Body Booking updates
    );

    @GET("bookings")
    Call<List<Booking>> getBookingsByClass(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("class_id") String classId,
            @Query("status") String status
    );
}