package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.AttendanceApi;
import com.crossfitarmyjym.app.data.api.BookingApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.AttendanceEntity;
import com.crossfitarmyjym.app.data.local.entity.dao.AttendanceDao;
import com.crossfitarmyjym.app.data.model.Attendance;
import com.crossfitarmyjym.app.data.model.AttendanceEntry;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRepository {

    private static final String TAG = "AttendanceRepository";
    private static final String ROSTER_SELECT =
            "id,class_id,user_id,status,profiles(id,email,full_name)";
    private static AttendanceRepository instance;

    private final BookingApi bookingApi;
    private final AttendanceApi attendanceApi;
    private final AttendanceDao attendanceDao;
    private final PreferencesManager preferencesManager;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public interface AttendanceCallback {
        void onSuccess(List<AttendanceEntry> entries);

        void onError(@NonNull String errorMessage);
    }

    public interface SaveCallback {
        void onSuccess();

        void onError(@NonNull String errorMessage);
    }

    private AttendanceRepository(Context context) {
        bookingApi = ApiClient.getBookingApi();
        attendanceApi = ApiClient.getAttendanceApi();
        attendanceDao = AppDatabase.getInstance(context).attendanceDao();
        preferencesManager = PreferencesManager.getInstance();
    }

    public static synchronized AttendanceRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AttendanceRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getClassAttendance(String classId, AttendanceCallback callback) {
        bookingApi.getBookingsByClass(
                "eq." + classId,
                "eq.confirmed",
                ROSTER_SELECT,
                "booked_at.asc"
        ).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call,
                                   @NonNull Response<List<Booking>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Не удалось загрузить клиентов (код "
                            + response.code() + ")");
                    return;
                }
                loadAttendanceMarks(classId, response.body(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call,
                                  @NonNull Throwable error) {
                Log.e(TAG, "Roster request failed", error);
                callback.onError("Ошибка сети. Проверьте подключение");
            }
        });
    }

    public void saveClassAttendance(String classId, List<AttendanceEntry> entries,
                                    SaveCallback callback) {
        String trainerId = preferencesManager.getUserId();
        if (trainerId == null || trainerId.isEmpty()) {
            callback.onError("Тренер не авторизован");
            return;
        }
        if (entries == null || entries.isEmpty()) {
            callback.onError("В занятии нет клиентов");
            return;
        }

        List<Attendance> payload = new ArrayList<>();
        for (AttendanceEntry entry : entries) {
            payload.add(entry.toAttendance(classId, trainerId));
        }

        attendanceApi.saveAttendance(
                "class_id,user_id",
                "resolution=merge-duplicates,return=representation",
                payload
        ).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(@NonNull Call<List<Attendance>> call,
                                   @NonNull Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheAttendance(response.body());
                    callback.onSuccess();
                } else {
                    callback.onError("Не удалось сохранить посещаемость (код "
                            + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Attendance>> call,
                                  @NonNull Throwable error) {
                Log.e(TAG, "Attendance save failed", error);
                callback.onError("Ошибка сети. Проверьте подключение");
            }
        });
    }

    private void loadAttendanceMarks(String classId, List<Booking> bookings,
                                     AttendanceCallback callback) {
        attendanceApi.getAttendanceByClass("eq." + classId, "*")
                .enqueue(new Callback<List<Attendance>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Attendance>> call,
                                           @NonNull Response<List<Attendance>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Не удалось загрузить посещаемость (код "
                                    + response.code() + ")");
                            return;
                        }
                        cacheAttendance(response.body());
                        callback.onSuccess(mergeRoster(bookings, response.body()));
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Attendance>> call,
                                          @NonNull Throwable error) {
                        Log.e(TAG, "Attendance request failed", error);
                        callback.onError("Ошибка сети. Проверьте подключение");
                    }
                });
    }

    private List<AttendanceEntry> mergeRoster(List<Booking> bookings,
                                              List<Attendance> attendance) {
        Map<String, Boolean> marks = new HashMap<>();
        for (Attendance item : attendance) {
            marks.put(item.getUserId(), item.isAttended());
        }

        List<AttendanceEntry> result = new ArrayList<>();
        for (Booking booking : bookings) {
            String userId = booking.getUserId();
            User user = booking.getUser();
            String name = user != null ? user.getFullName() : "";
            String email = user != null ? user.getEmail() : "";
            result.add(new AttendanceEntry(
                    userId,
                    name,
                    email,
                    Boolean.TRUE.equals(marks.get(userId))));
        }
        return result;
    }

    private void cacheAttendance(List<Attendance> attendance) {
        databaseExecutor.execute(() -> {
            List<AttendanceEntity> entities = new ArrayList<>();
            for (Attendance item : attendance) {
                if (item.getId() == null) {
                    continue;
                }
                AttendanceEntity entity = new AttendanceEntity(item.getId());
                entity.setClassId(item.getClassId());
                entity.setUserId(item.getUserId());
                entity.setAttended(item.isAttended());
                entity.setCheckInTime(item.getCheckInTime());
                entity.setMarkedBy(item.getMarkedBy());
                entity.setNotes(item.getNotes());
                entities.add(entity);
            }
            attendanceDao.insertAll(entities);
        });
    }
}
