package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.NewsPost;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NewsApi {

    @GET("news_posts")
    Call<List<NewsPost>> getNews(
            @Query("status") String status,
            @Query("order") String order
    );

    @GET("news_posts")
    Call<List<NewsPost>> getNewsById(@Query("id") String id);

    @GET("news_posts")
    Call<List<NewsPost>> getAdminNews(@Query("order") String order);

    @POST("news_posts")
    Call<List<NewsPost>> createNews(
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @PATCH("news_posts")
    Call<List<NewsPost>> updateNews(
            @Query("id") String id,
            @Query("select") String select,
            @Body Map<String, Object> fields
    );

    @DELETE("news_posts")
    Call<Void> deleteNews(@Query("id") String id);
}
