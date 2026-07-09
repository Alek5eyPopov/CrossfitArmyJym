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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleViewModel extends AndroidViewModel {

    private final ClassRepository classRepository;
    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<Date>> visibleDates = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<String> monthTitle = new MutableLiveData<>("");
    private final List<GymClass> loadedClasses = new ArrayList<>();
    private final MutableLiveData<List<GymClass>> classes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Set<String>> bookedClassIds = new MutableLiveData<>(Collections.emptySet());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        classRepository = ClassRepository.getInstance(application);
        bookingRepository = BookingRepository.getInstance(application);
        Date today = startOfDay(new Date());
        selectedDate.setValue(today);
        updateVisibleDates(today);
    }

    public LiveData<List<Date>> getVisibleDates() {
        return visibleDates;
    }

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<String> getMonthTitle() {
        return monthTitle;
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
        Date date = selectedDate.getValue();
        if (date == null) {
            date = startOfDay(new Date());
            selectedDate.setValue(date);
        }
        loadWeekFor(date, false);
    }

    public void refreshSchedule() {
        Date date = selectedDate.getValue();
        loadWeekFor(date != null ? date : startOfDay(new Date()), true);
    }

    public void selectDate(Date date) {
        if (date == null) return;
        Date cleanDate = startOfDay(date);
        selectedDate.setValue(cleanDate);
        monthTitle.setValue(formatDate(cleanDate, "LLLL yyyy"));
        if (!isDateInVisibleWeek(cleanDate)) {
            updateVisibleDates(cleanDate);
            loadWeekFor(cleanDate, false);
        } else {
            filterClassesForSelectedDate();
        }
    }

    public void shiftWeek(int weekOffset) {
        Date current = selectedDate.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current != null ? current : new Date());
        calendar.add(Calendar.DAY_OF_YEAR, weekOffset * 7);
        Date newDate = startOfDay(calendar.getTime());
        selectedDate.setValue(newDate);
        updateVisibleDates(newDate);
        loadWeekFor(newDate, false);
    }

    private void loadWeekFor(Date date, boolean forceRefresh) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        String weekStart = formatDate(weekStart(date), "yyyy-MM-dd");
        ClassRepository.ClassCallback callback = new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> gymClasses) {
                loadedClasses.clear();
                if (gymClasses != null) {
                    loadedClasses.addAll(gymClasses);
                }
                filterClassesForSelectedDate();
                loadBookingState();
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        };

        if (forceRefresh) {
            classRepository.refreshClassesFromDate(weekStart, callback);
        } else {
            classRepository.getClassesFromDate(weekStart, callback);
        }
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

    private void filterClassesForSelectedDate() {
        Date date = selectedDate.getValue();
        if (date == null) {
            classes.setValue(Collections.emptyList());
            return;
        }
        String selected = formatDate(date, "yyyy-MM-dd");
        List<GymClass> filtered = new ArrayList<>();
        for (GymClass gymClass : loadedClasses) {
            if (selected.equals(classDate(gymClass))) {
                filtered.add(gymClass);
            }
        }
        classes.setValue(filtered);
    }

    private String classDate(GymClass gymClass) {
        Date date = parseClassDate(gymClass != null ? gymClass.getScheduledStart() : null);
        return date == null ? "" : formatDate(date, "yyyy-MM-dd");
    }

    private void updateVisibleDates(Date anchorDate) {
        Date start = weekStart(anchorDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<Date> dates = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            dates.add(startOfDay(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        visibleDates.setValue(dates);
        monthTitle.setValue(formatDate(anchorDate, "LLLL yyyy"));
    }

    private boolean isDateInVisibleWeek(Date date) {
        List<Date> dates = visibleDates.getValue();
        if (dates == null) return false;
        String target = formatDate(date, "yyyy-MM-dd");
        for (Date visibleDate : dates) {
            if (target.equals(formatDate(visibleDate, "yyyy-MM-dd"))) {
                return true;
            }
        }
        return false;
    }

    private Date weekStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date != null ? date : new Date());
        int firstDay = calendar.getFirstDayOfWeek();
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDay) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return startOfDay(calendar.getTime());
    }

    private Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date != null ? date : new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date parseClassDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return null;
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(isoDate);
            } catch (Exception ignored) {
                // Try next Supabase timestamp format.
            }
        }
        return null;
    }

    private String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}
