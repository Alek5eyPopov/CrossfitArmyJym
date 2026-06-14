package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.AuthResponse;
import com.crossfitarmyjym.app.data.model.AuthUser;
import com.crossfitarmyjym.app.data.model.LoginRequest;
import com.crossfitarmyjym.app.data.model.RefreshTokenRequest;
import com.crossfitarmyjym.app.data.model.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("/auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("/auth/v1/signup")
    Call<AuthResponse> signup(
            @Query("redirect_to") String redirectTo,
            @Body SignupRequest request
    );

    @POST("/auth/v1/token?grant_type=refresh_token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("/auth/v1/logout")
    Call<Void> logout();

    @GET("/auth/v1/user")
    Call<AuthUser> getCurrentUser();
}
