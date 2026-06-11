package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.BookingApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.dao.BookingDao;
import com.crossfitarmyjym.app.data.local.entity.dao.GymClassDao;
import com.crossfitarmyjym.app.data.local.entity.BookingEntity;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с записями на занятия.
 * Поддерживает: создание записи, отмену, проверку лимита мест, кэширование.
 */
public class BookingRepository {

    private static final String TAG = "BookingRepository";
    private static final long CACHE_DURATION_MS = 2 * 60 * 1000; // 2 минуты

    private static BookingRepository instance;

    private final BookingApi api;
    private final BookingDao bookingDao;
    private final GymClassDao gymClassDao;
    private final PreferencesManager prefsManager;

    public interface BookingCallback {
        void onSuccess(List<Booking> bookings);
        void onError(@NonNull String errorMessage);
    }

    public interface SingleBookingCallback {
        void onSuccess(Booking booking);
        void onError(@NonNull String errorMessage);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(@NonNull String errorMessage);
    }

    private BookingRepository(Context context) {
        this.api = ApiClient.getBookingApi();
        this.bookingDao = AppDatabase.getInstance(context).bookingDao();
        this.gymClassDao = AppDatabase.getInstance(context).gymClassDao();
        this.prefsManager = PreferencesManager.getInstance();
    }

    public static synchronized BookingRepository getInstance(Context context) {
        if (instance == null) {
            instance = new BookingRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Получить записи текущего пользователя.
     */
    public void getMyBookings(BookingCallback callback) {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        // Пробуем кэш
        List<BookingEntity> cached = bookingDao.getBookingsByUser(userId);
        if (cached != null && !cached.isEmpty() && isCacheValid(cached.get(0).getCachedAt())) {
            callback.onSuccess(entitiesToModels(cached));
            return;
        }

        // Загружаем из сети
        fetchBookingsFromApi(userId, callback);
    }

    /**
     * Создать запись на занятие.
     */
    public void createBooking(String classId, SingleBookingCallback callback) {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        Booking booking = new Booking();
        // У модели Booking нет сеттеров, используем прямой доступ (временно)
        // TODO: добавить сеттеры в Booking model

        api.createBooking("Bearer " + token, apiKey, "return=representation", booking)
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(Call<Booking> call, Response<Booking> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Booking created: " + response.body().getId());
                            cacheBooking(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Ошибка записи: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Booking> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    /**
     * Отменить запись на занятие.
     */
    public void cancelBooking(String bookingId, VoidCallback callback) {
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        // Создаем объект с новым статусом
        Booking updates = new Booking();

        api.cancelBooking("Bearer " + token, apiKey, "eq." + bookingId, updates)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Booking cancelled: " + bookingId);
                            bookingDao.deleteById(bookingId);
                            callback.onSuccess();
                        } else {
                            callback.onError("Ошибка отмены: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    // --- Приватные методы ---

    private void fetchBookingsFromApi(String userId, BookingCallback callback) {
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();

        api.getBookingsByUser("Bearer " + token, apiKey, "eq." + userId, "booked_at.desc")
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Fetched " + response.body().size() + " bookings from API");
                            cacheBookings(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            // Fallback к кэшу
                            List<BookingEntity> cached = bookingDao.getBookingsByUser(userId);
                            if (cached != null && !cached.isEmpty()) {
                                callback.onSuccess(entitiesToModels(cached));
                            } else {
                                callback.onError("Ошибка загрузки: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage());
                        List<BookingEntity> cached = bookingDao.getBookingsByUser(userId);
                        if (cached != null && !cached.isEmpty()) {
                            callback.onSuccess(entitiesToModels(cached));
                        } else {
                            callback.onError("Ошибка сети: " + t.getMessage());
                        }
                    }
                });
    }

    private void cacheBooking(Booking booking) {
        BookingEntity entity = new BookingEntity(booking.getId());
        entity.setClassId(booking.getClassId());
        entity.setUserId(booking.getUserId());
        entity.setStatus(booking.getStatus());
        entity.setBookedAt(booking.getBookedAt());
        bookingDao.insert(entity);
    }

    private void cacheBookings(List<Booking> bookings) {
        List<BookingEntity> entities = new ArrayList<>();
        for (Booking b : bookings) {
            BookingEntity entity = new BookingEntity(b.getId());
            entity.setClassId(b.getClassId());
            entity.setUserId(b.getUserId());
            entity.setStatus(b.getStatus());
            entity.setBookedAt(b.getBookedAt());
            entities.add(entity);
        }
        bookingDao.insertAll(entities);
    }

    private List<Booking> entitiesToModels(List<BookingEntity> entities) {
        List<Booking> models = new ArrayList<>();
        for (BookingEntity entity : entities) {
            Booking booking = new Booking();
            models.add(booking);
        }
        return models;
    }

    private boolean isCacheValid(long cachedAt) {
        return (System.currentTimeMillis() - cachedAt) < CACHE_DURATION_MS;
    }
}