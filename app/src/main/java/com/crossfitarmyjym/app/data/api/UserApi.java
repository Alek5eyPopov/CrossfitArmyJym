package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

/**
 * Retrofit интерфейс для работы с таблицей profiles в Supabase.
 * Использует PostgREST API.
 */
public interface UserApi {

    /**
     * Получение профиля пользователя по ID.
     * GET /profiles?id=eq.{userId}
     *
     * @param userId UUID пользователя
     * @return Call со списком User (ожидаем 1 элемент)
     */
    @GET("profiles?id=eq.{userId}")
    Call<List<User>> getUserById(@Path("userId") String userId);

    /**
     * Получение всех пользователей (только для админа).
     * GET /profiles
     *
     * @return Call со списком всех пользователей
     */
    @GET("profiles")
    Call<List<User>> getAllUsers();

    /**
     * Обновление профиля пользователя.
     * PATCH /profiles?id=eq.{userId}
     *
     * @param userId UUID пользователя
     * @param user данные для обновления
     * @return Call без тела ответа
     */
    @PATCH("profiles?id=eq.{userId}")
    Call<Void> updateUser(@Path("userId") String userId, User user);

    /**
     * Удаление профиля пользователя (только для админа).
     * DELETE /profiles?id=eq.{userId}
     *
     * @param userId UUID пользователя
     * @return Call без тела ответа
     */
    @DELETE("profiles?id=eq.{userId}")
    Call<Void> deleteUser(@Path("userId") String userId);

    /**
     * Получение пользователей по группе.
     * GET /profiles?group_id=eq.{groupId}
     *
     * @param groupId UUID группы
     * @return Call со списком пользователей группы
     */
    @GET("profiles?group_id=eq.{groupId}")
    Call<List<User>> getUsersByGroup(@Path("groupId") String groupId);

    /**
     * Получение пользователей по роли.
     * GET /profiles?role=eq.{role}
     *
     * @param role роль (athlete, trainer, admin)
     * @return Call со списком пользователей с указанной ролью
     */
    @GET("profiles?role=eq.{role}")
    Call<List<User>> getUsersByRole(@Path("role") String role);
}