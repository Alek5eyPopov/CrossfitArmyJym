package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.LeaderboardEntry;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.PersonalRecordRequest;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.ResultRequest;
import com.crossfitarmyjym.app.data.model.TrainingTask;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodCompositionRequest;
import com.crossfitarmyjym.app.data.model.WodTaskCompositionRequest;

import java.util.List;
import java.util.Map;

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
    Call<List<Exercise>> getExercises(
            @Query("is_active") String active,
            @Query("order") String order
    );

    @POST("exercises")
    Call<List<Exercise>> createExercise(
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @GET("load_types")
    Call<List<LoadType>> getLoadTypes(
            @Query("is_active") String active,
            @Query("order") String order
    );

    @POST("load_types")
    Call<List<LoadType>> createLoadType(
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @GET("training_tasks")
    Call<List<TrainingTask>> getTrainingTasks(
            @Query("is_active") String active,
            @Query("order") String order,
            @Query("select") String select
    );

    @POST("training_tasks")
    Call<List<TrainingTask>> createTrainingTask(
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @POST("rpc/create_wod_with_exercises")
    Call<Wod> createWod(@Body WodCompositionRequest request);

    @POST("rpc/create_wod_with_tasks")
    Call<Wod> createWodWithTasks(@Body WodTaskCompositionRequest request);

    @POST("rpc/submit_wod_result")
    Call<Result> submitResult(@Body ResultRequest request);

    @POST("rpc/get_wod_leaderboard")
    Call<List<LeaderboardEntry>> getLeaderboard(@Body ResultRequest request);

    @GET("results")
    Call<List<Result>> getMyResults(
            @Query("user_id") String userId,
            @Query("order") String order,
            @Query("select") String select
    );

    @POST("rpc/submit_personal_record")
    Call<PersonalRecord> submitPersonalRecord(@Body PersonalRecordRequest request);

    @GET("personal_records")
    Call<List<PersonalRecord>> getPersonalRecords(
            @Query("user_id") String userId,
            @Query("order") String order,
            @Query("select") String select
    );

    @GET("personal_record_bests")
    Call<List<PersonalRecord>> getPersonalRecordBests(
            @Query("user_id") String userId,
            @Query("order") String order
    );
}
