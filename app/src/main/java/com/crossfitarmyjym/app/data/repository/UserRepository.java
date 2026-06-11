package com.crossfitarmyjym.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.UserApi;
import com.crossfitarmyjym.app.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с пользователями (профилями).
 * Использует Repository Pattern для абстракции источника данных.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static UserRepository instance;

    private final UserApi userApi;

    private UserRepository() {
        userApi = ApiClient.getUserApi();
    }

    /**
     * Получение singleton instance.
     * @return UserRepository instance
     */
    @NonNull
    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Получение профиля пользователя по ID.
     * @param userId UUID пользователя
     * @param callback результат операции
     */
    public void getUserById(@NonNull String userId, @NonNull UserCallback callback) {
        Log.d(TAG, "Getting user by ID: " + userId);
        
        userApi.getUserById(userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    Log.e(TAG, "Get user failed: " + response.code());
                    callback.onError("Пользователь не найден");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get user request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Получение всех пользователей (только для админа).
     * @param callback результат операции
     */
    public void getAllUsers(@NonNull UserListCallback callback) {
        Log.d(TAG, "Getting all users");
        
        userApi.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Get all users failed: " + response.code());
                    callback.onError("Ошибка получения пользователей: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get all users request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Обновление профиля пользователя.
     * @param userId UUID пользователя
     * @param user данные для обновления
     * @param callback результат операции
     */
    public void updateUser(@NonNull String userId, @NonNull User user, @NonNull VoidCallback callback) {
        Log.d(TAG, "Updating user: " + userId);
        
        userApi.updateUser(userId, user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User updated successfully");
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Update user failed: " + response.code());
                    callback.onError("Ошибка обновления: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Update user request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Удаление профиля пользователя (только для админа).
     * @param userId UUID пользователя
     * @param callback результат операции
     */
    public void deleteUser(@NonNull String userId, @NonNull VoidCallback callback) {
        Log.d(TAG, "Deleting user: " + userId);
        
        userApi.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User deleted successfully");
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Delete user failed: " + response.code());
                    callback.onError("Ошибка удаления: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Delete user request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Получение пользователей по группе.
     * @param groupId UUID группы
     * @param callback результат операции
     */
    public void getUsersByGroup(@NonNull String groupId, @NonNull UserListCallback callback) {
        Log.d(TAG, "Getting users by group: " + groupId);
        
        userApi.getUsersByGroup(groupId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Get users by group failed: " + response.code());
                    callback.onError("Ошибка получения пользователей группы");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get users by group request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Получение пользователей по роли.
     * @param role роль (athlete, trainer, admin)
     * @param callback результат операции
     */
    public void getUsersByRole(@NonNull String role, @NonNull UserListCallback callback) {
        Log.d(TAG, "Getting users by role: " + role);
        
        userApi.getUsersByRole(role).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Get users by role failed: " + response.code());
                    callback.onError("Ошибка получения пользователей");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get users by role request failed", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ==================== Callback интерфейсы ====================

    /**
     * Callback для получения одного пользователя.
     */
    public interface UserCallback {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String errorMessage);
    }

    /**
     * Callback для получения списка пользователей.
     */
    public interface UserListCallback {
        void onSuccess(@NonNull List<User> users);
        void onError(@NonNull String errorMessage);
    }

    /**
     * Callback для операций без возвращаемого значения.
     */
    public interface VoidCallback {
        void onSuccess();
        void onError(@NonNull String errorMessage);
    }
}