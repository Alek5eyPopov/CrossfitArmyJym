package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Wod;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * API интерфейс для работы с WOD.
 * Использует Supabase PostgREST API (таблица wods).
 */
public interface WodApi {

    /**
     * Получить WOD на указанную дату.
     */
    @GET("wods")
    Call<List<Wod>> getWodByDate(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("scheduled_date") String date,
            @Query("limit") int limit
    );

    /**
     * Получить WOD по ID.
     */
    @GET("wods")
    Call<List<Wod>> getWodById(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey,
            @Query("id") String id
    );
}