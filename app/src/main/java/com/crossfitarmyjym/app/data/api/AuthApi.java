package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.AuthResponse;
import com.crossfitarmyjym.app.data.model.LoginRequest;
import com.crossfitarmyjym.app.data.model.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit интерфейс для работы с Supabase Auth API.
 * Все методы асинхронные (возвращают Call).
 */
public interface AuthApi {

    /**
     * Вход пользователя (получение токена).
     * POST /auth/v1/token?grant_type=password
     *
     * @param request запрос с email и password
     * @return Call с AuthResponse
     */
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Регистрация нового пользователя.
     * POST /auth/v1/signup
     *
     * @param request запрос с email, password и опционально fullName
     * @return Call с AuthResponse
     */
    @POST("auth/v1/signup")
    Call<AuthResponse> signup(@Body SignupRequest request);

    /**
     * Выход пользователя (аннулирование токена).
     * POST /auth/v1/logout
     *
     * @return Call без тела ответа
     */
    @POST("auth/v1/logout")
    Call<Void> logout();

    /**
     * Получение данных текущего пользователя.
     * GET /auth/v1/user
     *
     * @return Call с AuthResponse (содержит user)
     */
    @POST("auth/v1/user")
    Call<AuthResponse> getCurrentUser();
}