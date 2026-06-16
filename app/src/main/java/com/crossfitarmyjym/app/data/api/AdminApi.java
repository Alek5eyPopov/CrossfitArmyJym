package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Attendance;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.TrainingTask;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AdminApi {

    @GET("profiles")
    Call<List<User>> getUsers(@Query("order") String order);

    @PATCH("profiles")
    Call<List<User>> updateUser(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @GET("groups")
    Call<List<Group>> getGroups(@Query("order") String order);

    @POST("groups")
    Call<List<Group>> createGroup(@Query("select") String select, @Body Map<String, Object> fields);

    @PATCH("groups")
    Call<List<Group>> updateGroup(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("groups")
    Call<Void> deleteGroup(@Query("id") String id);

    @GET("classes")
    Call<List<GymClass>> getClasses(@Query("order") String order);

    @POST("classes")
    Call<List<GymClass>> createClass(@Query("select") String select,
                                     @Body Map<String, Object> fields);

    @PATCH("classes")
    Call<List<GymClass>> updateClass(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("classes")
    Call<Void> deleteClass(@Query("id") String id);

    @GET("wods")
    Call<List<Wod>> getWods(@Query("order") String order);

    @PATCH("wods")
    Call<List<Wod>> updateWod(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("wods")
    Call<Void> deleteWod(@Query("id") String id);

    @GET("exercises")
    Call<List<Exercise>> getExercises(@Query("order") String order);

    @POST("exercises")
    Call<List<Exercise>> createExercise(@Query("select") String select,
                                        @Body Map<String, Object> fields);

    @PATCH("exercises")
    Call<List<Exercise>> updateExercise(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("exercises")
    Call<Void> deleteExercise(@Query("id") String id);

    @GET("load_types")
    Call<List<LoadType>> getLoadTypes(@Query("order") String order);

    @POST("load_types")
    Call<List<LoadType>> createLoadType(@Query("select") String select,
                                        @Body Map<String, Object> fields);

    @PATCH("load_types")
    Call<List<LoadType>> updateLoadType(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("load_types")
    Call<Void> deleteLoadType(@Query("id") String id);

    @GET("training_tasks")
    Call<List<TrainingTask>> getTrainingTasks(
            @Query("order") String order,
            @Query("select") String select
    );

    @POST("training_tasks")
    Call<List<TrainingTask>> createTrainingTask(@Query("select") String select,
                                                @Body Map<String, Object> fields);

    @PATCH("training_tasks")
    Call<List<TrainingTask>> updateTrainingTask(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("training_tasks")
    Call<Void> deleteTrainingTask(@Query("id") String id);

    @GET("bookings")
    Call<List<Booking>> getBookings(@Query("status") String status);

    @GET("attendance")
    Call<List<Attendance>> getAttendance();

    @GET("results")
    Call<List<Result>> getResults();
}
