package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface UserApi {

    @GET("profiles")
    Call<List<User>> getUserById(@Query("id") String userIdFilter);

    @GET("profiles")
    Call<List<User>> getAllUsers();

    @PATCH("profiles")
    Call<Void> updateUser(@Query("id") String userIdFilter, @Body User user);

    @DELETE("profiles")
    Call<Void> deleteUser(@Query("id") String userIdFilter);

    @GET("profiles")
    Call<List<User>> getUsersByGroup(@Query("group_id") String groupIdFilter);

    @GET("profiles")
    Call<List<User>> getUsersByRole(@Query("role") String roleFilter);
}
