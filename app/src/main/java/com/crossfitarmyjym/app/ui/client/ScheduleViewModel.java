package com.crossfitarmyjym.app.ui.client;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.repository.BookingRepository;
import com.crossfitarmyjym.app.data.repository.ClassRepository;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleViewModel extends AndroidViewModel {

    private final ClassRepository classRepository;
    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<GymClass>> classes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Set<String>> bookedClassIds = new MutableLiveData<>(Collections.emptySet());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        classRepository = ClassRepository.getInstance(application);
        bookingRepository = BookingRepository.getInstance(application);
    }

    public LiveData<List<GymClass>> getClasses() {
        return classes;
    }

    public LiveData<Set<String>> getBookedClassIds() {
        return bookedClassIds;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    public void loadSchedule() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        classRepository.getClassesFromDate(today(), new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> gymClasses) {
                classes.setValue(gymClasses);
                loadBookingState();
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void refreshSchedule() {
        isLoading.setValue(true);
        classRepository.refreshClassesFromDate(today(), new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> gymClasses) {
                classes.setValue(gymClasses);
                loadBookingState();
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void bookClass(String classId) {
        isLoading.setValue(true);
        bookingRepository.createBooking(classId, new BookingRepository.SingleBookingCallback() {
            @Override
            public void onSuccess(Booking booking) {
                bookingStatus.setValue("Вы записаны на занятие");
                refreshSchedule();
            }

            @Override
            public void onError(@NonNull String error) {
                bookingStatus.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    private void loadBookingState() {
        bookingRepository.refreshMyBookings(new BookingRepository.BookingCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                Set<String> ids = new HashSet<>();
                for (Booking booking : bookings) {
                    if (booking.isConfirmed() && booking.getClassId() != null) {
                        ids.add(booking.getClassId());
                    }
                }
                bookedClassIds.setValue(ids);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}
