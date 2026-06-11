package com.crossfitarmyjym.app.ui.client;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.repository.BookingRepository;

import java.util.List;

/**
 * ViewModel для экрана моих записей.
 * Загружает список записей пользователя, поддерживает отмену.
 */
public class BookingsViewModel extends AndroidViewModel {

    private static final String TAG = "BookingsViewModel";

    private final BookingRepository bookingRepository;

    private final MutableLiveData<List<Booking>> bookings = new MutableLiveData<>();
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

    /**
     * Загрузить записи текущего пользователя.
     */
    public void loadMyBookings() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        bookingRepository.getMyBookings(new BookingRepository.BookingCallback() {
            @Override
            public void onSuccess(List<Booking> bookingList) {
                Log.d(TAG, "Loaded " + bookingList.size() + " bookings");
                bookings.setValue(bookingList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading bookings: " + error);
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Отменить запись на занятие.
     */
    public void cancelBooking(String bookingId) {
        isLoading.setValue(true);
        cancelStatus.setValue(null);

        bookingRepository.cancelBooking(bookingId, new BookingRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Booking cancelled: " + bookingId);
                cancelStatus.setValue("Запись отменена");
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Cancel failed: " + error);
                cancelStatus.setValue("Ошибка: " + error);
                isLoading.setValue(false);
            }
        });
    }
}