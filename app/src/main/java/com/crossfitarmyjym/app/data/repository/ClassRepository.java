package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.GymClassApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.dao.GymClassDao;
import com.crossfitarmyjym.app.data.local.entity.GymClassEntity;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с расписанием занятий.
 * Получает данные из Supabase и кэширует в Room.
 */
public class ClassRepository {

    private static final String TAG = "ClassRepository";
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 минут

    private static ClassRepository instance;

    private final GymClassApi api;
    private final GymClassDao dao;
    private final PreferencesManager prefsManager;

    public interface ClassCallback {
        void onSuccess(List<GymClass> classes);
        void onError(@NonNull String errorMessage);
    }

    private ClassRepository(Context context) {
        this.api = ApiClient.getGymClassApi();
        this.dao = AppDatabase.getInstance(context).gymClassDao();
        this.prefsManager = PreferencesManager.getInstance();
    }

    public static synchronized ClassRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ClassRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Получить список занятий от указанной даты.
     * Сначала пытается загрузить из кэша, потом из сети.
     */
    public void getClassesFromDate(String date, ClassCallback callback) {
        List<GymClassEntity> cached = dao.getClassesFromDate(date);
        if (cached != null && !cached.isEmpty() && isCacheValid(cached.get(0).getCachedAt())) {
            Log.d(TAG, "Loading classes from cache, count: " + cached.size());
            callback.onSuccess(entitiesToModels(cached));
            return;
        }
        fetchClassesFromApi(date, callback);
    }

    /**
     * Получить занятия по тренеру.
     */
    public void getClassesByTrainer(String trainerId, ClassCallback callback) {
        List<GymClassEntity> cached = dao.getClassesByTrainer(trainerId);
        if (cached != null && !cached.isEmpty() && isCacheValid(cached.get(0).getCachedAt())) {
            callback.onSuccess(entitiesToModels(cached));
            return;
        }

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getClassesByTrainer("Bearer " + token, apiKey, trainerId, "scheduled_start.asc")
                .enqueue(new Callback<List<GymClass>>() {
                    @Override
                    public void onResponse(Call<List<GymClass>> call, Response<List<GymClass>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Fetched " + response.body().size() + " classes from API");
                            cacheClasses(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Ошибка загрузки: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GymClass>> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    private void fetchClassesFromApi(String date, ClassCallback callback) {
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getClassesFromDate(
                "Bearer " + token,
                apiKey,
                "gte." + date,
                "scheduled_start.asc",
                "eq.scheduled"
        ).enqueue(new Callback<List<GymClass>>() {
            @Override
            public void onResponse(Call<List<GymClass>> call, Response<List<GymClass>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched " + response.body().size() + " classes from API");
                    cacheClasses(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Ошибка загрузки: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GymClass>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                List<GymClassEntity> cached = dao.getClassesFromDate(date);
                if (cached != null && !cached.isEmpty()) {
                    callback.onSuccess(entitiesToModels(cached));
                } else {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    private void cacheClasses(List<GymClass> classes) {
        List<GymClassEntity> entities = new ArrayList<>();
        for (GymClass gymClass : classes) {
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
    }

    private List<GymClass> entitiesToModels(List<GymClassEntity> entities) {
        List<GymClass> models = new ArrayList<>();
        for (GymClassEntity entity : entities) {
            GymClass gymClass = new GymClass();
            models.add(gymClass);
        }
        return models;
    }

    private boolean isCacheValid(long cachedAt) {
        return (System.currentTimeMillis() - cachedAt) < CACHE_DURATION_MS;
    }
}