package com.crossfitarmyjym.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.data.SessionExpiry;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.AuthApi;
import com.crossfitarmyjym.app.data.api.UserApi;
import com.crossfitarmyjym.app.data.model.AuthResponse;
import com.crossfitarmyjym.app.data.model.AuthUser;
import com.crossfitarmyjym.app.data.model.LoginRequest;
import com.crossfitarmyjym.app.data.model.RefreshTokenRequest;
import com.crossfitarmyjym.app.data.model.SignupRequest;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    public static final String EMAIL_CONFIRMATION_REDIRECT =
            "crossfitarmyjym://email-confirmed";
    private static AuthRepository instance;

    private final AuthApi authApi;
    private final UserApi userApi;
    private final PreferencesManager preferencesManager;

    private AuthRepository() {
        authApi = ApiClient.getAuthApi();
        userApi = ApiClient.getUserApi();
        preferencesManager = PreferencesManager.getInstance();
    }

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

    public void login(@NonNull String email, @NonNull String password,
                      @NonNull AuthCallback callback) {
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                AuthResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    establishSession(body, callback);
                    return;
                }
                callback.onError(authError("Ошибка входа", response));
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable error) {
                callback.onError(networkError(error));
            }
        });
    }

    public void signup(@NonNull String email, @NonNull String password,
                       @Nullable String fullName, @NonNull AuthCallback callback) {
        authApi.signup(
                        EMAIL_CONFIRMATION_REDIRECT,
                        new SignupRequest(email, password, fullName)
                )
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (!response.isSuccessful() || body == null || body.getUser() == null) {
                            callback.onError(authError("Ошибка регистрации", response));
                            return;
                        }
                        if (!body.hasSession()) {
                            clearLocalSession();
                            callback.onEmailConfirmationRequired();
                            return;
                        }
                        establishSession(body, callback);
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                          @NonNull Throwable error) {
                        callback.onError(networkError(error));
                    }
                });
    }

    public void logout(@NonNull AuthCallback callback) {
        authApi.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                clearLocalSession();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable error) {
                Log.w(TAG, "Remote logout failed; local session cleared", error);
                clearLocalSession();
                callback.onSuccess(null);
            }
        });
    }

    public void getCurrentUser(@NonNull UserCallback callback) {
        if (!preferencesManager.isAuthorized()) {
            clearLocalSession();
            callback.onError("Сессия отсутствует");
            return;
        }

        if (SessionExpiry.shouldRefresh(
                preferencesManager.getAccessTokenExpiresAt(),
                System.currentTimeMillis())) {
            refreshSession(callback);
        } else {
            requestAuthUser(callback, true);
        }
    }

    private void establishSession(@NonNull AuthResponse response,
                                  @NonNull AuthCallback callback) {
        AuthUser authUser = response.getUser();
        if (authUser == null || authUser.getId().isEmpty()) {
            clearLocalSession();
            callback.onError("Сервер вернул некорректные данные пользователя");
            return;
        }

        saveTokens(response);
        loadProfile(authUser, new ProfileCallback() {
            @Override
            public void onSuccess(@NonNull User profile) {
                saveProfile(profile);
                callback.onSuccess(profile);
            }

            @Override
            public void onError(@NonNull String message) {
                clearLocalSession();
                callback.onError(message);
            }
        });
    }

    private void refreshSession(@NonNull UserCallback callback) {
        String refreshToken = preferencesManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            clearLocalSession();
            callback.onError("Сессия истекла");
            return;
        }

        authApi.refreshToken(new RefreshTokenRequest(refreshToken))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.hasSession()) {
                            saveTokens(body);
                            requestAuthUser(callback, false);
                            return;
                        }
                        clearLocalSession();
                        callback.onError("Сессия истекла. Войдите снова");
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                          @NonNull Throwable error) {
                        callback.onError(networkError(error));
                    }
                });
    }

    private void requestAuthUser(@NonNull UserCallback callback, boolean retryOnUnauthorized) {
        authApi.getCurrentUser().enqueue(new Callback<AuthUser>() {
            @Override
            public void onResponse(@NonNull Call<AuthUser> call,
                                   @NonNull Response<AuthUser> response) {
                AuthUser authUser = response.body();
                if (response.isSuccessful() && authUser != null && !authUser.getId().isEmpty()) {
                    loadProfile(authUser, new ProfileCallback() {
                        @Override
                        public void onSuccess(@NonNull User profile) {
                            saveProfile(profile);
                            callback.onSuccess(profile);
                        }

                        @Override
                        public void onError(@NonNull String message) {
                            clearLocalSession();
                            callback.onError(message);
                        }
                    });
                } else if (response.code() == 401 && retryOnUnauthorized) {
                    refreshSession(callback);
                } else {
                    clearLocalSession();
                    callback.onError("Не удалось восстановить сессию");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthUser> call, @NonNull Throwable error) {
                callback.onError(networkError(error));
            }
        });
    }

    private void loadProfile(@NonNull AuthUser authUser,
                             @NonNull ProfileCallback callback) {
        userApi.getUserById("eq." + authUser.getId())
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<User>> call,
                                           @NonNull Response<List<User>> response) {
                        List<User> profiles = response.body();
                        if (!response.isSuccessful() || profiles == null || profiles.isEmpty()) {
                            callback.onError("Профиль пользователя не найден");
                            return;
                        }

                        User profile = profiles.get(0);
                        if (!profile.isActive()) {
                            callback.onError("Аккаунт заблокирован");
                            return;
                        }
                        callback.onSuccess(profile);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<User>> call,
                                          @NonNull Throwable error) {
                        callback.onError(networkError(error));
                    }
                });
    }

    private void saveTokens(@NonNull AuthResponse response) {
        preferencesManager.saveAuthToken(response.getAccessToken());
        preferencesManager.saveRefreshToken(response.getRefreshToken());
        preferencesManager.saveAccessTokenExpiresAt(SessionExpiry.calculateExpiresAt(
                System.currentTimeMillis(), response.getExpiresIn()));
    }

    private void saveProfile(@NonNull User profile) {
        preferencesManager.saveUserId(
                profile.getId() != null ? profile.getId().toString() : null);
        preferencesManager.saveUserRole(profile.getRole());
        preferencesManager.saveUserEmail(profile.getEmail());
        preferencesManager.saveUserName(profile.getFullName());
        preferencesManager.saveGroupId(
                profile.getGroupId() != null ? profile.getGroupId().toString() : null);
        preferencesManager.saveIsLoggedIn(true);
    }

    private void clearLocalSession() {
        preferencesManager.clearAll();
    }

    private String authError(String prefix, Response<?> response) {
        String serverMessage = readAuthErrorMessage(response);
        if ("Email not confirmed".equalsIgnoreCase(serverMessage)
                || "email_not_confirmed".equalsIgnoreCase(serverMessage)) {
            return "Email не подтверждён. Откройте письмо Supabase или подтвердите пользователя в панели";
        }
        if ("Invalid login credentials".equalsIgnoreCase(serverMessage)
                || "invalid_credentials".equalsIgnoreCase(serverMessage)) {
            return "Неверный email или пароль";
        }
        if (serverMessage != null && !serverMessage.isEmpty()) {
            return prefix + ": " + serverMessage;
        }
        return prefix + " (код " + response.code() + ")";
    }

    @Nullable
    private String readAuthErrorMessage(Response<?> response) {
        if (response.errorBody() == null) {
            return null;
        }
        try {
            JsonObject json = JsonParser.parseString(response.errorBody().string())
                    .getAsJsonObject();
            if (json.has("message")) {
                return json.get("message").getAsString();
            }
            if (json.has("error_description")) {
                return json.get("error_description").getAsString();
            }
            if (json.has("error_code")) {
                return json.get("error_code").getAsString();
            }
            if (json.has("code")) {
                return json.get("code").getAsString();
            }
        } catch (IOException | RuntimeException error) {
            Log.w(TAG, "Could not parse Supabase auth error", error);
        }
        return null;
    }

    private String networkError(Throwable error) {
        Log.e(TAG, "Network request failed", error);
        return "Ошибка сети. Проверьте подключение";
    }

    public boolean isAuthorized() {
        return preferencesManager.isAuthorized();
    }

    public interface AuthCallback {
        void onSuccess(@Nullable User user);

        void onError(@NonNull String errorMessage);

        default void onEmailConfirmationRequired() {
            onError("Подтвердите email и затем войдите в приложение");
        }
    }

    public interface UserCallback {
        void onSuccess(@NonNull User user);

        void onError(@NonNull String errorMessage);
    }

    private interface ProfileCallback {
        void onSuccess(@NonNull User profile);

        void onError(@NonNull String message);
    }
}
