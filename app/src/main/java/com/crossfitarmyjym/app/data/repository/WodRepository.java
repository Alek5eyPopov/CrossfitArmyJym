package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.WodApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.dao.WodDao;
import com.crossfitarmyjym.app.data.local.entity.WodEntity;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с WOD (тренировками дня).
 * Получает данные из Supabase и кэширует в Room.
 */
public class WodRepository {

    private static final String TAG = "WodRepository";
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 минут

    private static WodRepository instance;

    private final WodApi api;
    private final WodDao dao;
    private final PreferencesManager prefsManager;

    public interface WodCallback {
        void onSuccess(Wod wod);
        void onError(@NonNull String errorMessage);
    }

    public interface WodListCallback {
        void onSuccess(List<Wod> wods);
        void onError(@NonNull String errorMessage);
    }

    private WodRepository(Context context) {
        this.api = ApiClient.getWodApi();
        this.dao = AppDatabase.getInstance(context).wodDao();
        this.prefsManager = PreferencesManager.getInstance();
    }

    public static synchronized WodRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WodRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Получить WOD на сегодня.
     */
    public void getTodaysWod(WodCallback callback) {
        String today = getTodayDateString();
        Log.d(TAG, "Getting WOD for date: " + today);

        // Пробуем кэш
        WodEntity cached = dao.getWodByDate(today);
        if (cached != null && isCacheValid(cached.getCachedAt())) {
            Wod wod = entityToModel(cached);
            if (wod != null) {
                callback.onSuccess(wod);
                return;
            }
        }

        // Загружаем из сети
        fetchWodFromApi(today, callback);
    }

    /**
     * Получить WOD по ID.
     */
    public void getWodById(String wodId, WodCallback callback) {
        WodEntity cached = dao.getWodById(wodId);
        if (cached != null && isCacheValid(cached.getCachedAt())) {
            Wod wod = entityToModel(cached);
            if (wod != null) {
                callback.onSuccess(wod);
                return;
            }
        }

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getWodById("Bearer " + token, apiKey, "eq." + wodId)
                .enqueue(new Callback<List<Wod>>() {
                    @Override
                    public void onResponse(Call<List<Wod>> call, Response<List<Wod>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Wod wod = response.body().get(0);
                            cacheWod(wod);
                            callback.onSuccess(wod);
                        } else {
                            callback.onError("WOD не найден: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Wod>> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    /**
     * Получить все WOD (для тренера/админа).
     */
    public void getAllWods(WodListCallback callback) {
        List<WodEntity> cached = dao.getAllWods();
        if (cached != null && !cached.isEmpty() && isCacheValid(cached.get(0).getCachedAt())) {
            callback.onSuccess(entitiesToModels(cached));
            return;
        }

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getWodByDate("Bearer " + token, apiKey, "", 50)
                .enqueue(new Callback<List<Wod>>() {
                    @Override
                    public void onResponse(Call<List<Wod>> call, Response<List<Wod>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheWods(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Ошибка загрузки: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Wod>> call, Throwable t) {
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    // --- Приватные методы ---

    private void fetchWodFromApi(String date, WodCallback callback) {
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getWodByDate("Bearer " + token, apiKey, "eq." + date, 1)
                .enqueue(new Callback<List<Wod>>() {
                    @Override
                    public void onResponse(Call<List<Wod>> call, Response<List<Wod>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Wod wod = response.body().get(0);
                            cacheWod(wod);
                            callback.onSuccess(wod);
                        } else {
                            callback.onError("WOD на сегодня не найден");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Wod>> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        // Fallback к кэшу
                        WodEntity cached = dao.getWodByDate(date);
                        if (cached != null) {
                            Wod wod = entityToModel(cached);
                            if (wod != null) {
                                callback.onSuccess(wod);
                                return;
                            }
                        }
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    private void cacheWod(Wod wod) {
        if (wod.getId() == null) return;
        WodEntity entity = new WodEntity(wod.getId());
        entity.setName(wod.getName());
        entity.setFormat(wod.getFormat());
        entity.setTrainerId(wod.getTrainerId());
        entity.setScheduledDate(wod.getScheduledDate());
        entity.setTimeCapSeconds(wod.getTimeCapSeconds());
        entity.setNotes(wod.getNotes());
        entity.setCreatedAt(wod.getCreatedAt());
        dao.insert(entity);
    }

    private void cacheWods(List<Wod> wods) {
        List<WodEntity> entities = new ArrayList<>();
        for (Wod wod : wods) {
            if (wod.getId() == null) continue;
            WodEntity entity = new WodEntity(wod.getId());
            entity.setName(wod.getName());
            entity.setFormat(wod.getFormat());
            entity.setTrainerId(wod.getTrainerId());
            entity.setScheduledDate(wod.getScheduledDate());
            entity.setTimeCapSeconds(wod.getTimeCapSeconds());
            entity.setNotes(wod.getNotes());
            entity.setCreatedAt(wod.getCreatedAt());
            entities.add(entity);
        }
        if (!entities.isEmpty()) {
            dao.insertAll(entities);
        }
    }

    private Wod entityToModel(WodEntity entity) {
        if (entity == null) return null;
        Wod wod = new Wod();
        return wod;
    }

    private List<Wod> entitiesToModels(List<WodEntity> entities) {
        List<Wod> models = new ArrayList<>();
        for (WodEntity entity : entities) {
            Wod wod = entityToModel(entity);
            if (wod != null) models.add(wod);
        }
        return models;
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isCacheValid(long cachedAt) {
        return (System.currentTimeMillis() - cachedAt) < CACHE_DURATION_MS;
    }
}