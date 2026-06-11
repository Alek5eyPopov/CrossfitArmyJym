package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.GymClass;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API интерфейс для работы с расписанием занятий.
 * Использует Supabase PostgREST API (таблица classes).
 */
public interface GymClassApi {

    /**
     * Получить список занятий от указанной даты.
     */
    @GET("classes")
    Call<List<GymClass>> getClassesFromDate(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("scheduled_start") String gteDate,
            @Query("order") String order,
            @Query("status") String status
    );

    /**
     * Получить занятия по тренеру.
     */
    @GET("classes")
    Call<List<GymClass>> getClassesByTrainer(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("trainer_id") String trainerId,
            @Query("order") String order
    );

    /**
     * Получить занятие по ID.
     */
    @GET("classes")
    Call<List<GymClass>> getClassById(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("id") String id
    );
}