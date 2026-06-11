package com.crossfitarmyjym.app.data;

import android.util.Log;

import com.crossfitarmyjym.app.BuildConfig;

/**
 * Конфигурация для подключения к Supabase.
 * Использует сгенерированные поля BuildConfig для безопасного хранения ключей.
 */
public final class SupabaseConfig {

    private static final String TAG = "SupabaseConfig";

    // Приватный конструктор для предотвращения создания экземпляров
    private SupabaseConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Получение URL Supabase проекта.
     * @return URL вида https://{project}.supabase.co/rest/v1/
     */
    public static String getSupabaseUrl() {
        String url = BuildConfig.SUPABASE_URL;
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "SUPABASE_URL is not configured in BuildConfig");
        }
        return url;
    }

    /**
     * Получение анонимного ключа Supabase.
     * @return Анонимный ключ для доступа к API
     */
    public static String getSupabaseAnonKey() {
        String key = BuildConfig.SUPABASE_ANON_KEY;
        if (key == null || key.isEmpty()) {
            Log.e(TAG, "SUPABASE_ANON_KEY is not configured in BuildConfig");
        }
        return key;
    }

    /**
     * Проверка, настроены ли ключи Supabase.
     * @return true, если ключи настроены
     */
    public static boolean isConfigured() {
        return !getSupabaseUrl().isEmpty() && !getSupabaseAnonKey().isEmpty();
    }

    /**
     * Получение заголовка Authorization для запросов.
     * @param token JWT токен пользователя
     * @return Заголовок Authorization
     */
    public static String getAuthorizationHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Получение заголовка apikey для запросов.
     * @return Заголовок apikey
     */
    public static String getApiKeyHeader() {
        return getSupabaseAnonKey();
    }

    /**
     * Логирование информации о конфигурации (без секретных данных).
     */
    public static void logConfiguration() {
        Log.d(TAG, "Supabase Configuration:");
        Log.d(TAG, "  URL configured: " + !getSupabaseUrl().isEmpty());
        Log.d(TAG, "  Anon Key configured: " + !getSupabaseAnonKey().isEmpty());
    }
}