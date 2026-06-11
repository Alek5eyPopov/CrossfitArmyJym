package com.crossfitarmyjym.app.data.api;

import android.util.Log;

import com.crossfitarmyjym.app.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Клиент для подключения к Supabase API.
 * Использует Retrofit для REST запросов.
 * Singleton pattern для обеспечения единого экземпляра.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;

    private static Retrofit retrofit;
    private static AuthApi authApi;
    private static UserApi userApi;
    private static GymClassApi gymClassApi;
    private static BookingApi bookingApi;
    private static AttendanceApi attendanceApi;
    private static WodApi wodApi;
    private static AdminApi adminApi;

    // Приватный конструктор
    private ApiClient() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Получение Retrofit instance.
     * Создает новый instance при первом вызове.
     * @return Retrofit instance
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = createRetrofit();
        }
        return retrofit;
    }

    /**
     * Создание Retrofit instance с настройками.
     * @return настроенный Retrofit
     */
    private static Retrofit createRetrofit() {
        Log.d(TAG, "Creating Retrofit client");
        
        // Логирование для отладки
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> Log.d(TAG, "HTTP: " + message)
        );
        loggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        // Настройка OkHttpClient
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthInterceptor());

        return new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Получение базового URL из конфигурации.
     * @return базовый URL Supabase
     */
    private static String getBaseUrl() {
        // Импортируем конфигурацию
        try {
            Class<?> configClass = Class.forName("com.crossfitarmyjym.app.data.SupabaseConfig");
            java.lang.reflect.Method getUrlMethod = configClass.getMethod("getSupabaseUrl");
            return (String) getUrlMethod.invoke(null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get Supabase URL from config", e);
            return "";
        }
    }

    /**
     * Получение AuthApi для работы с аутентификацией.
     * @return AuthApi instance
     */
    public static AuthApi getAuthApi() {
        if (authApi == null) {
            authApi = getClient().create(AuthApi.class);
        }
        return authApi;
    }

    /**
     * Получение UserApi для работы с пользователями.
     * @return UserApi instance
     */
    public static UserApi getUserApi() {
        if (userApi == null) {
            userApi = getClient().create(UserApi.class);
        }
        return userApi;
    }

    /**
     * Получение GymClassApi для работы с расписанием.
     * @return GymClassApi instance
     */
    public static GymClassApi getGymClassApi() {
        if (gymClassApi == null) {
            gymClassApi = getClient().create(GymClassApi.class);
        }
        return gymClassApi;
    }

    /**
     * Получение BookingApi для работы с записями.
     * @return BookingApi instance
     */
    public static BookingApi getBookingApi() {
        if (bookingApi == null) {
            bookingApi = getClient().create(BookingApi.class);
        }
        return bookingApi;
    }

    public static AttendanceApi getAttendanceApi() {
        if (attendanceApi == null) {
            attendanceApi = getClient().create(AttendanceApi.class);
        }
        return attendanceApi;
    }

    /**
     * Получение WodApi для работы с WOD.
     * @return WodApi instance
     */
    public static WodApi getWodApi() {
        if (wodApi == null) {
            wodApi = getClient().create(WodApi.class);
        }
        return wodApi;
    }

    public static AdminApi getAdminApi() {
        if (adminApi == null) {
            adminApi = getClient().create(AdminApi.class);
        }
        return adminApi;
    }

    /**
     * Очистка Retrofit instance (используется при выходе).
     */
    public static void clear() {
        retrofit = null;
        authApi = null;
        userApi = null;
        gymClassApi = null;
        bookingApi = null;
        attendanceApi = null;
        wodApi = null;
        adminApi = null;
        Log.d(TAG, "ApiClient cleared");
    }
}
