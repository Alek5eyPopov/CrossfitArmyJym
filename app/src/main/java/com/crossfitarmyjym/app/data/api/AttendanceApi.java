package com.crossfitarmyjym.app.data.api;

import com.crossfitarmyjym.app.data.model.Attendance;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AttendanceApi {

    @GET("attendance")
    Call<List<Attendance>> getAttendanceByClass(
            @Query("class_id") String classId,
            @Query("select") String select
    );

    @POST("attendance")
    Call<List<Attendance>> saveAttendance(
            @Query("on_conflict") String conflictColumns,
            @Header("Prefer") String prefer,
            @Body List<Attendance> attendance
    );
}
