package com.crossfitarmyjym.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.AuthApi;
import com.crossfitarmyjym.app.data.model.AuthResponse;
import com.crossfitarmyjym.app.data.model.LoginRequest;
import com.crossfitarmyjym.app.data.model.SignupRequest;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с аутентификацией.
 * Использует Repository Pattern для абстракции источника данных.
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;

    private final AuthApi authApi;
    private final PreferencesManager preferencesManager;

    private AuthRepository() {
        authApi = ApiClient.getAuthApi();
        preferencesManager = PreferencesManager.getInstance();
    }

    /**
     * Получение singleton instance.
     * @return AuthRepository instance
     */
    @NonNull
    public static AuthRepository getInstance() {
        if (instance == null) {
            synchronized (AuthRepository.class) {
                if (instance == null) {
                    instance = new AuthRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Вход пользователя.
     * @param email email пользователя
     * @param password пароль
     * @param callback результат операции
     */
    public void login(@NonNull String email, @NonNull String password, 
                      @NonNull AuthCallback callback) {
        Log.d(TAG, "Login attempt for email: " + email);
        
        LoginRequest request = new LoginRequest(email, password);
        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, 
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    handleSuccessfulAuth(authResponse, callback);
                } else {
                    Log.e(TAG, "Login failed: " + response.code() + " - " + response.message());
                    callback.onError("Ошибка входа: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Login request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Регистрация нового пользователя.
     * @param email email
     * @param password пароль
     * @param fullName полное имя (опционально)
     * @param callback результат операции
     */
    public void signup(@NonNull String email, @NonNull String password, 
                       @Nullable String fullName, @NonNull AuthCallback callback) {
        Log.d(TAG, "Signup attempt for email: " + email);
        
        SignupRequest request = new SignupRequest(email, password, fullName);
        authApi.signup(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, 
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    handleSuccessfulAuth(authResponse, callback);
                } else {
                    Log.e(TAG, "Signup failed: " + response.code() + " - " + response.message());
                    callback.onError("Ошибка регистрации: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Signup request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Выход пользователя.
     * @param callback результат операции
     */
    public void logout(@NonNull AuthCallback callback) {
        Log.d(TAG, "Logout attempt");
        
        authApi.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Очищаем сохраненные данные независимо от ответа сервера
                preferencesManager.clearAll();
                Log.d(TAG, "Logout successful");
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Все равно очищаем данные
                preferencesManager.clearAll();
                Log.w(TAG, "Logout request failed, but cleared local data", t);
                callback.onSuccess(null);
            }
        });
    }

    /**
     * Получение данных текущего пользователя.
     * @param callback результат операции
     */
    public void getCurrentUser(@NonNull UserCallback callback) {
        Log.d(TAG, "Getting current user");
        
        authApi.getCurrentUser().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, 
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Пользователь не найден");
                    }
                } else {
                    Log.e(TAG, "Get user failed: " + response.code());
                    callback.onError("Ошибка получения данных: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Get user request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Обработка успешной аутентификации.
     */
    private void handleSuccessfulAuth(@NonNull AuthResponse authResponse, 
                                      @NonNull AuthCallback callback) {
        User user = authResponse.getUser();
        if (user == null) {
            callback.onError("Некорректный ответ сервера");
            return;
        }

        // Сохраняем данные в PreferencesManager
        preferencesManager.saveAuthToken(authResponse.getAccessToken());
        preferencesManager.saveRefreshToken(authResponse.getRefreshToken());
        preferencesManager.saveUserId(user.getId() != null ? user.getId().toString() : null);
        preferencesManager.saveUserRole(user.getRole());
        preferencesManager.saveUserEmail(user.getEmail());
        preferencesManager.saveUserName(user.getFullName());
        preferencesManager.saveGroupId(user.getGroupId() != null ? user.getGroupId().toString() : null);
        preferencesManager.saveIsLoggedIn(true);

        Log.d(TAG, "Auth successful for user: " + user.getEmail());
        callback.onSuccess(user);
    }

    /**
     * Проверка, авторизован ли пользователь.
     * @return true если пользователь авторизован
     */
    public boolean isAuthorized() {
        return preferencesManager.isAuthorized();
    }

    // ==================== Callback интерфейсы ====================

    /**
     * Callback для операций аутентификации.
     */
    public interface AuthCallback {
        void onSuccess(@Nullable User user);
        void onError(@NonNull String errorMessage);
    }

    /**
     * Callback для получения пользователя.
     */
    public interface UserCallback {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String errorMessage);
    }
}