package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingRpcRequest {

    @SerializedName("p_class_id")
    private String classId;

    @SerializedName("p_booking_id")
    private String bookingId;

    public static BookingRpcRequest forClass(String classId) {
        BookingRpcRequest request = new BookingRpcRequest();
        request.classId = classId;
        return request;
    }

    public static BookingRpcRequest forBooking(String bookingId) {
        BookingRpcRequest request = new BookingRpcRequest();
        request.bookingId = bookingId;
        return request;
    }
}
