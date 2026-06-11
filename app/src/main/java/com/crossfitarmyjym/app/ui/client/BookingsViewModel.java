package com.crossfitarmyjym.app.ui.client;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.repository.BookingRepository;

import java.util.Collections;
import java.util.List;

public class BookingsViewModel extends AndroidViewModel {

    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<Booking>> bookings =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> cancelStatus = new MutableLiveData<>();

    public BookingsViewModel(@NonNull Application application) {
        super(application);
        bookingRepository = BookingRepository.getInstance(application);
    }

    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getCancelStatus() {
        return cancelStatus;
    }

    public void loadMyBookings() {
        load(false);
    }

    public void refreshMyBookings() {
        load(true);
    }

    public void cancelBooking(String bookingId) {
        isLoading.setValue(true);
        cancelStatus.setValue(null);
        bookingRepository.cancelBooking(bookingId, new BookingRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                cancelStatus.setValue("Запись отменена");
                refreshMyBookings();
            }

            @Override
            public void onError(@NonNull String error) {
                cancelStatus.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    private void load(boolean forceRefresh) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        BookingRepository.BookingCallback callback = new BookingRepository.BookingCallback() {
            @Override
            public void onSuccess(List<Booking> bookingList) {
                bookings.setValue(bookingList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        };
        if (forceRefresh) {
            bookingRepository.refreshMyBookings(callback);
        } else {
            bookingRepository.getMyBookings(callback);
        }
    }
}
