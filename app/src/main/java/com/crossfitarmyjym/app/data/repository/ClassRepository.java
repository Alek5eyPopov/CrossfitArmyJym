package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.GymClassApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.GymClassEntity;
import com.crossfitarmyjym.app.data.local.entity.dao.GymClassDao;
import com.crossfitarmyjym.app.data.model.GymClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassRepository {

    private static final String TAG = "ClassRepository";
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000;
    private static ClassRepository instance;

    private final GymClassApi api;
    private final GymClassDao dao;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ClassCallback {
        void onSuccess(List<GymClass> classes);

        void onError(@NonNull String errorMessage);
    }

    private ClassRepository(Context context) {
        api = ApiClient.getGymClassApi();
        dao = AppDatabase.getInstance(context).gymClassDao();
    }

    public static synchronized ClassRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ClassRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getClassesFromDate(String date, ClassCallback callback) {
        databaseExecutor.execute(() -> {
            List<GymClassEntity> cached = dao.getClassesFromDate(date);
            if (isValidCache(cached)) {
                postSuccess(callback, entitiesToModels(cached));
            } else {
                fetchClassesFromApi(date, callback);
            }
        });
    }

    public void refreshClassesFromDate(String date, ClassCallback callback) {
        fetchClassesFromApi(date, callback);
    }

    public void getClassesByTrainer(String trainerId, ClassCallback callback) {
        api.getClassesByTrainer("eq." + trainerId, "scheduled_start.asc")
                .enqueue(new Callback<List<GymClass>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GymClass>> call,
                                           @NonNull Response<List<GymClass>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheClasses(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Ошибка загрузки занятий (код "
                                    + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<GymClass>> call,
                                          @NonNull Throwable error) {
                        callback.onError("Ошибка сети. Проверьте подключение");
                    }
                });
    }

    private void fetchClassesFromApi(String date, ClassCallback callback) {
        api.getClassesFromDate(
                "(scheduled_start.gte." + date
                        + ",scheduled_start.lt." + getDateAfterDays(date, 7) + ")",
                "scheduled_start.asc",
                "eq.scheduled"
        ).enqueue(new Callback<List<GymClass>>() {
            @Override
            public void onResponse(@NonNull Call<List<GymClass>> call,
                                   @NonNull Response<List<GymClass>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheClasses(response.body());
                    callback.onSuccess(response.body());
                } else {
                    loadStaleCacheOrError(date, callback,
                            "Ошибка загрузки расписания (код " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GymClass>> call,
                                  @NonNull Throwable error) {
                Log.e(TAG, "Schedule request failed", error);
                loadStaleCacheOrError(date, callback,
                        "Ошибка сети. Проверьте подключение");
            }
        });
    }

    private void cacheClasses(List<GymClass> classes) {
        databaseExecutor.execute(() -> {
            List<GymClassEntity> entities = new ArrayList<>();
            for (GymClass gymClass : classes) {
                if (gymClass.getId() == null) {
                    continue;
                }
                GymClassEntity entity = new GymClassEntity(gymClass.getId());
                entity.setTrainerId(gymClass.getTrainerId());
                entity.setScheduledStart(gymClass.getScheduledStart());
                entity.setScheduledEnd(gymClass.getScheduledEnd());
                entity.setMaxCapacity(gymClass.getMaxCapacity());
                entity.setCurrentBookings(gymClass.getCurrentBookings());
                entity.setLocation(gymClass.getLocation());
                entity.setStatus(gymClass.getStatus());
                entities.add(entity);
            }
            dao.insertAll(entities);
        });
    }

    private void loadStaleCacheOrError(String date, ClassCallback callback, String error) {
        databaseExecutor.execute(() -> {
            List<GymClassEntity> cached = dao.getClassesFromDate(date);
            if (cached != null && !cached.isEmpty()) {
                postSuccess(callback, entitiesToModels(cached));
            } else {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    private List<GymClass> entitiesToModels(List<GymClassEntity> entities) {
        List<GymClass> models = new ArrayList<>();
        for (GymClassEntity entity : entities) {
            GymClass gymClass = new GymClass();
            gymClass.setId(entity.getId());
            gymClass.setTrainerId(entity.getTrainerId());
            gymClass.setScheduledStart(entity.getScheduledStart());
            gymClass.setScheduledEnd(entity.getScheduledEnd());
            gymClass.setMaxCapacity(entity.getMaxCapacity());
            gymClass.setCurrentBookings(entity.getCurrentBookings());
            gymClass.setLocation(entity.getLocation());
            gymClass.setStatus(entity.getStatus());
            models.add(gymClass);
        }
        return models;
    }

    private boolean isValidCache(List<GymClassEntity> cached) {
        return cached != null
                && !cached.isEmpty()
                && System.currentTimeMillis() - cached.get(0).getCachedAt() < CACHE_DURATION_MS;
    }

    private void postSuccess(ClassCallback callback, List<GymClass> classes) {
        mainHandler.post(() -> callback.onSuccess(classes));
    }

    private String getDateAfterDays(String date, int days) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsed = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed != null ? parsed : new Date());
            calendar.add(Calendar.DAY_OF_YEAR, days);
            return format.format(calendar.getTime());
        } catch (Exception error) {
            return date;
        }
    }
}
