package com.crossfitarmyjym.app.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.MyApplication;

/**
 * Менеджер для безопасного хранения предпочтений пользователя.
 * Использует EncryptedSharedPreferences для хранения чувствительных данных.
 * Singleton pattern.
 */
public final class PreferencesManager {

    private static final String TAG = "PreferencesManager";
    private static final String PREFS_FILE = "app_preferences";
    
    // Ключи для EncryptedSharedPreferences (чувствительные данные)
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    
    // Ключи для обычных SharedPreferences (не чувствительные данные)
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_GROUP_ID = "group_id";

    private static PreferencesManager instance;
    
    private final SharedPreferences encryptedPrefs;
    private final SharedPreferences regularPrefs;

    private PreferencesManager() {
        // Получаем EncryptedSharedPreferences из MyApplication
        encryptedPrefs = MyApplication.getInstance().getEncryptedSharedPreferences();
        // Обычные SharedPreferences для не чувствительных данных
        regularPrefs = MyApplication.getInstance().getSharedPreferences(
                PREFS_FILE, 
                Context.MODE_PRIVATE
        );
        Log.d(TAG, "PreferencesManager initialized");
    }

    /**
     * Получение singleton instance.
     * @return PreferencesManager instance
     */
    @NonNull
    public static PreferencesManager getInstance() {
        if (instance == null) {
            synchronized (PreferencesManager.class) {
                if (instance == null) {
                    instance = new PreferencesManager();
                }
            }
        }
        return instance;
    }

    // ==================== Encrypted Preferences (чувствительные данные) ====================

    /**
     * Сохранение JWT токена.
     * @param token JWT токен
     */
    public void saveAuthToken(@Nullable String token) {
        encryptedPrefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
        Log.d(TAG, "Auth token saved");
    }

    /**
     * Получение JWT токена.
     * @return JWT токен или null
     */
    @Nullable
    public String getAuthToken() {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Сохранение refresh токена.
     * @param token refresh токен
     */
    public void saveRefreshToken(@Nullable String token) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
        Log.d(TAG, "Refresh token saved");
    }

    /**
     * Получение refresh токена.
     * @return refresh токен или null
     */
    @Nullable
    public String getRefreshToken() {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Сохранение ID пользователя.
     * @param userId UUID пользователя
     */
    public void saveUserId(@Nullable String userId) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply();
        Log.d(TAG, "User ID saved: " + userId);
    }

    /**
     * Получение ID пользователя.
     * @return ID пользователя или null
     */
    @Nullable
    public String getUserId() {
        return encryptedPrefs.getString(KEY_USER_ID, null);
    }

    // ==================== Regular Preferences (не чувствительные данные) ====================

    /**
     * Сохранение роли пользователя.
     * @param role роль (athlete, trainer, admin)
     */
    public void saveUserRole(@Nullable String role) {
        regularPrefs.edit().putString(KEY_USER_ROLE, role).apply();
        Log.d(TAG, "User role saved: " + role);
    }

    /**
     * Получение роли пользователя.
     * @return роль пользователя или null
     */
    @Nullable
    public String getUserRole() {
        return regularPrefs.getString(KEY_USER_ROLE, null);
    }

    /**
     * Сохранение email пользователя.
     * @param email email
     */
    public void saveUserEmail(@Nullable String email) {
        regularPrefs.edit().putString(KEY_USER_EMAIL, email).apply();
        Log.d(TAG, "User email saved");
    }

    /**
     * Получение email пользователя.
     * @return email или null
     */
    @Nullable
    public String getUserEmail() {
        return regularPrefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Сохранение имени пользователя.
     * @param name полное имя
     */
    public void saveUserName(@Nullable String name) {
        regularPrefs.edit().putString(KEY_USER_NAME, name).apply();
        Log.d(TAG, "User name saved");
    }

    /**
     * Получение имени пользователя.
     * @return имя или null
     */
    @Nullable
    public String getUserName() {
        return regularPrefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Сохранение статуса входа.
     * @param isLoggedIn true если пользователь вошел
     */
    public void saveIsLoggedIn(boolean isLoggedIn) {
        regularPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
        Log.d(TAG, "Login status saved: " + isLoggedIn);
    }

    /**
     * Получение статуса входа.
     * @return true если пользователь вошел
     */
    public boolean isLoggedIn() {
        return regularPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Сохранение ID группы пользователя.
     * @param groupId UUID группы
     */
    public void saveGroupId(@Nullable String groupId) {
        regularPrefs.edit().putString(KEY_GROUP_ID, groupId).apply();
        Log.d(TAG, "Group ID saved: " + groupId);
    }

    /**
     * Получение ID группы пользователя.
     * @return ID группы или null
     */
    @Nullable
    public String getGroupId() {
        return regularPrefs.getString(KEY_GROUP_ID, null);
    }

    // ==================== Общие методы ====================

    /**
     * Очистка всех данных (при выходе).
     */
    public void clearAll() {
        encryptedPrefs.edit().clear().apply();
        regularPrefs.edit().clear().apply();
        Log.d(TAG, "All preferences cleared");
    }

    /**
     * Проверка, авторизован ли пользователь.
     * @return true если пользователь авторизован
     */
    public boolean isAuthorized() {
        return isLoggedIn() && getAuthToken() != null;
    }
}