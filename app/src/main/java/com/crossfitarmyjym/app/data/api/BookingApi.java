package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.BookingRpcRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BookingApi {

    @GET("bookings")
    Call<List<Booking>> getBookingsByUser(
            @Query("user_id") String userId,
            @Query("order") String order,
            @Query("select") String select
    );

    @POST("rpc/book_class")
    Call<Booking> bookClass(@Body BookingRpcRequest request);

    @POST("rpc/cancel_booking")
    Call<Booking> cancelBooking(@Body BookingRpcRequest request);

    @GET("bookings")
    Call<List<Booking>> getBookingsByClass(
            @Query("class_id") String classId,
            @Query("status") String status,
            @Query("select") String select,
            @Query("order") String order
    );
}
