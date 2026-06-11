package com.crossfitarmyjym.app.ui.client;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.repository.BookingRepository;
import com.crossfitarmyjym.app.data.repository.ClassRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel для экрана расписания занятий.
 * Загружает список занятий на неделю, управляет записью.
 */
public class ScheduleViewModel extends AndroidViewModel {

    private static final String TAG = "ScheduleViewModel";

    private final ClassRepository classRepository;
    private final BookingRepository bookingRepository;

    private final MutableLiveData<List<GymClass>> classes = new MutableLiveData<>();
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    /**
     * Загрузить расписание на ближайшие 7 дней.
     */
    public void loadSchedule() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String today = getTodayDateString();
        Log.d(TAG, "Loading schedule from date: " + today);

        classRepository.getClassesFromDate(today, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> gymClasses) {
                Log.d(TAG, "Loaded " + gymClasses.size() + " classes");
                classes.setValue(gymClasses);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading schedule: " + error);
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Записаться на занятие.
     */
    public void bookClass(String classId) {
        isLoading.setValue(true);
        bookingStatus.setValue(null);

        bookingRepository.createBooking(classId, new BookingRepository.SingleBookingCallback() {
            @Override
            public void onSuccess(com.crossfitarmyjym.app.data.model.Booking booking) {
                Log.d(TAG, "Successfully booked class: " + classId);
                bookingStatus.setValue("Вы записаны!");
                isLoading.setValue(false);
                // Перезагружаем расписание, чтобы обновить количество мест
                loadSchedule();
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Booking failed: " + error);
                bookingStatus.setValue("Ошибка: " + error);
                isLoading.setValue(false);
            }
        });
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}