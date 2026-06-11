package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.LeaderboardEntry;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.ResultRequest;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodCompositionRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WodApi {

    @GET("wods")
    Call<List<Wod>> getWodByDate(
            @Query("scheduled_date") String date,
            @Query("limit") int limit,
            @Query("select") String select
    );

    @GET("wods")
    Call<List<Wod>> getWodById(
            @Query("id") String id,
            @Query("select") String select
    );

    @GET("wods")
    Call<List<Wod>> getWods(
            @Query("limit") int limit,
            @Query("select") String select
    );

    @GET("groups")
    Call<List<Group>> getTrainerGroups(
            @Query("trainer_id") String trainerId,
            @Query("is_active") String active,
            @Query("order") String order
    );

    @GET("exercises")
    Call<List<Exercise>> getExercises(@Query("order") String order);

    @POST("rpc/create_wod_with_exercises")
    Call<Wod> createWod(@Body WodCompositionRequest request);

    @POST("rpc/submit_wod_result")
    Call<Result> submitResult(@Body ResultRequest request);

    @POST("rpc/get_wod_leaderboard")
    Call<List<LeaderboardEntry>> getLeaderboard(@Body ResultRequest request);

    @GET("results")
    Call<List<Result>> getMyResults(
            @Query("user_id") String userId,
            @Query("order") String order
    );
}
