package com.crossfitarmyjym.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class BookingModelsTest {

    private final Gson gson = new Gson();

    @Test
    public void classRpcRequest_usesSupabaseParameterName() {
        assertEquals(
                "{\"p_class_id\":\"class-1\"}",
                gson.toJson(BookingRpcRequest.forClass("class-1")));
    }

    @Test
    public void cancelRpcRequest_usesSupabaseParameterName() {
        assertEquals(
                "{\"p_booking_id\":\"booking-1\"}",
                gson.toJson(BookingRpcRequest.forBooking("booking-1")));
    }

    @Test
    public void availableSlots_reflectCapacity() {
        GymClass gymClass = new GymClass();
        gymClass.setMaxCapacity(10);
        gymClass.setCurrentBookings(9);

        assertEquals(1, gymClass.getAvailableSlots());
        assertTrue(gymClass.hasAvailableSlots());

        gymClass.setCurrentBookings(10);
        assertFalse(gymClass.hasAvailableSlots());
    }
}
