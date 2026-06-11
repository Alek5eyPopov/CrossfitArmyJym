package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.BookingApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.BookingEntity;
import com.crossfitarmyjym.app.data.local.entity.GymClassEntity;
import com.crossfitarmyjym.app.data.local.entity.dao.BookingDao;
import com.crossfitarmyjym.app.data.local.entity.dao.GymClassDao;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.BookingRpcRequest;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {

    private static final String TAG = "BookingRepository";
    private static final long CACHE_DURATION_MS = 2 * 60 * 1000;
    private static final String BOOKING_SELECT =
            "*,classes(id,scheduled_start,scheduled_end,location,max_capacity,current_bookings,status)";
    private static BookingRepository instance;

    private final BookingApi api;
    private final BookingDao bookingDao;
    private final GymClassDao gymClassDao;
    private final PreferencesManager preferencesManager;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        api = ApiClient.getBookingApi();
        AppDatabase database = AppDatabase.getInstance(context);
        bookingDao = database.bookingDao();
        gymClassDao = database.gymClassDao();
        preferencesManager = PreferencesManager.getInstance();
    }

    public static synchronized BookingRepository getInstance(Context context) {
        if (instance == null) {
            instance = new BookingRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getMyBookings(BookingCallback callback) {
        String userId = preferencesManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        databaseExecutor.execute(() -> {
            List<BookingEntity> cached = bookingDao.getBookingsByUser(userId);
            if (isValidCache(cached)) {
                postSuccess(callback, entitiesToModels(cached));
            } else {
                fetchBookingsFromApi(userId, callback);
            }
        });
    }

    public void refreshMyBookings(BookingCallback callback) {
        String userId = preferencesManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            callback.onError("Пользователь не авторизован");
            return;
        }
        fetchBookingsFromApi(userId, callback);
    }

    public void createBooking(String classId, SingleBookingCallback callback) {
        if (classId == null || classId.isEmpty()) {
            callback.onError("Занятие не выбрано");
            return;
        }

        api.bookClass(BookingRpcRequest.forClass(classId))
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(@NonNull Call<Booking> call,
                                           @NonNull Response<Booking> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheBooking(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(bookingError(response, "Не удалось записаться"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Booking> call,
                                          @NonNull Throwable error) {
                        callback.onError("Ошибка сети. Проверьте подключение");
                    }
                });
    }

    public void cancelBooking(String bookingId, VoidCallback callback) {
        if (bookingId == null || bookingId.isEmpty()) {
            callback.onError("Запись не выбрана");
            return;
        }

        api.cancelBooking(BookingRpcRequest.forBooking(bookingId))
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(@NonNull Call<Booking> call,
                                           @NonNull Response<Booking> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheBooking(response.body());
                            callback.onSuccess();
                        } else {
                            callback.onError(bookingError(response, "Не удалось отменить запись"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Booking> call,
                                          @NonNull Throwable error) {
                        callback.onError("Ошибка сети. Проверьте подключение");
                    }
                });
    }

    private void fetchBookingsFromApi(String userId, BookingCallback callback) {
        api.getBookingsByUser("eq." + userId, "booked_at.desc", BOOKING_SELECT)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call,
                                           @NonNull Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheBookings(response.body());
                            callback.onSuccess(response.body());
                        } else {
                            loadStaleCacheOrError(userId, callback,
                                    "Ошибка загрузки записей (код " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call,
                                          @NonNull Throwable error) {
                        Log.e(TAG, "Bookings request failed", error);
                        loadStaleCacheOrError(userId, callback,
                                "Ошибка сети. Проверьте подключение");
                    }
                });
    }

    private void cacheBooking(Booking booking) {
        databaseExecutor.execute(() -> bookingDao.insert(toEntity(booking)));
    }

    private void cacheBookings(List<Booking> bookings) {
        databaseExecutor.execute(() -> {
            List<BookingEntity> entities = new ArrayList<>();
            for (Booking booking : bookings) {
                if (booking.getId() != null) {
                    entities.add(toEntity(booking));
                }
            }
            bookingDao.insertAll(entities);
        });
    }

    private BookingEntity toEntity(Booking booking) {
        BookingEntity entity = new BookingEntity(booking.getId());
        entity.setClassId(booking.getClassId());
        entity.setUserId(booking.getUserId());
        entity.setStatus(booking.getStatus());
        entity.setBookedAt(booking.getBookedAt());
        return entity;
    }

    private List<Booking> entitiesToModels(List<BookingEntity> entities) {
        List<Booking> models = new ArrayList<>();
        for (BookingEntity entity : entities) {
            Booking booking = new Booking();
            booking.setId(entity.getId());
            booking.setClassId(entity.getClassId());
            booking.setUserId(entity.getUserId());
            booking.setStatus(entity.getStatus());
            booking.setBookedAt(entity.getBookedAt());
            GymClassEntity cachedClass = gymClassDao.getClassById(entity.getClassId());
            if (cachedClass != null) {
                booking.setGymClass(toModel(cachedClass));
            }
            models.add(booking);
        }
        return models;
    }

    private GymClass toModel(GymClassEntity entity) {
        GymClass gymClass = new GymClass();
        gymClass.setId(entity.getId());
        gymClass.setTrainerId(entity.getTrainerId());
        gymClass.setScheduledStart(entity.getScheduledStart());
        gymClass.setScheduledEnd(entity.getScheduledEnd());
        gymClass.setMaxCapacity(entity.getMaxCapacity());
        gymClass.setCurrentBookings(entity.getCurrentBookings());
        gymClass.setLocation(entity.getLocation());
        gymClass.setStatus(entity.getStatus());
        return gymClass;
    }

    private void loadStaleCacheOrError(String userId, BookingCallback callback, String error) {
        databaseExecutor.execute(() -> {
            List<BookingEntity> cached = bookingDao.getBookingsByUser(userId);
            if (cached != null && !cached.isEmpty()) {
                postSuccess(callback, entitiesToModels(cached));
            } else {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    private boolean isValidCache(List<BookingEntity> cached) {
        return cached != null
                && !cached.isEmpty()
                && System.currentTimeMillis() - cached.get(0).getCachedAt() < CACHE_DURATION_MS;
    }

    private void postSuccess(BookingCallback callback, List<Booking> bookings) {
        mainHandler.post(() -> callback.onSuccess(bookings));
    }

    private String bookingError(Response<?> response, String fallback) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                JsonObject json = JsonParser.parseString(errorBody.string()).getAsJsonObject();
                String message = json.has("message") ? json.get("message").getAsString() : "";
                if (message.contains("capacity")) {
                    return "На занятии больше нет свободных мест";
                }
                if (message.contains("not open")) {
                    return "Запись на это занятие закрыта";
                }
                if (message.contains("not found")) {
                    return "Занятие или запись не найдены";
                }
            } catch (Exception ignored) {
                Log.w(TAG, "Unable to parse booking error");
            }
        }
        return fallback + " (код " + response.code() + ")";
    }
}
