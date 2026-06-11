package com.crossfitarmyjym.app.data.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor для добавления заголовков авторизации к запросам.
 * Добавляет apikey и Authorization заголовки к каждому запросу.
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private static final String HEADER_APIKEY = "apikey";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private final PreferencesManager preferencesManager;

    public AuthInterceptor() {
        // Получаем PreferencesManager для доступа к токену
        this.preferencesManager = PreferencesManager.getInstance();
    }

    @Override
    @NonNull
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        
        // Получаем токен из PreferencesManager
        String token = preferencesManager.getAuthToken();
        
        // Логирование (без токена)
        Log.d(TAG, "Intercepting request: " + original.url());
        
        // Строим новый запрос с заголовками
        Request.Builder requestBuilder = original.newBuilder()
                .header(HEADER_APIKEY, SupabaseConfig.getSupabaseAnonKey());
        
        // Добавляем Authorization заголовок только если есть токен
        if (token != null && !token.isEmpty()) {
            requestBuilder.header(HEADER_AUTHORIZATION, SupabaseConfig.getAuthorizationHeader(token));
            Log.d(TAG, "Added Authorization header");
        } else {
            Log.d(TAG, "No auth token, skipping Authorization header");
        }
        
        Request request = requestBuilder.build();
        
        return chain.proceed(request);
    }
}