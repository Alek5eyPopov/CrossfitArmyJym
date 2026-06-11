package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ViewModel для статистики (админ-панель).
 * Загружает данные для графиков: посещаемость, популярность занятий, пользователи.
 */
public class AdminStatisticsViewModel extends AndroidViewModel {

    private static final String TAG = "AdminStatsVM";

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<Integer> totalUsers = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalClasses = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalBookings = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<float[]> attendanceData = new MutableLiveData<>();
    private final MutableLiveData<String[]> attendanceLabels = new MutableLiveData<>();

    public AdminStatisticsViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<Integer> getTotalUsers() { return totalUsers; }
    public LiveData<Integer> getTotalClasses() { return totalClasses; }
    public LiveData<Integer> getTotalBookings() { return totalBookings; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<float[]> getAttendanceData() { return attendanceData; }
    public LiveData<String[]> getAttendanceLabels() { return attendanceLabels; }

    /**
     * Загрузить статистику.
     */
    public void loadStatistics() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String baseUrl = SupabaseConfig.getSupabaseUrl();

        // Загружаем количество пользователей
        fetchCount(baseUrl + "profiles?select=id", token, apiKey, count -> {
            totalUsers.postValue(count);
            if (totalUsers.getValue() != null && totalClasses.getValue() != null && totalBookings.getValue() != null) {
                isLoading.postValue(false);
            }
        });

        // Загружаем количество занятий
        fetchCount(baseUrl + "classes?select=id", token, apiKey, count -> {
            totalClasses.postValue(count);
            if (totalUsers.getValue() != null && totalClasses.getValue() != null && totalBookings.getValue() != null) {
                isLoading.postValue(false);
            }
        });

        // Загружаем количество записей
        fetchCount(baseUrl + "bookings?select=id&status=eq.confirmed", token, apiKey, count -> {
            totalBookings.postValue(count);
            if (totalUsers.getValue() != null && totalClasses.getValue() != null && totalBookings.getValue() != null) {
                isLoading.postValue(false);
            }
        });
    }

    private void fetchCount(String url, String token, String apiKey, CountCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("apikey", apiKey)
                .header("Prefer", "count=exact")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error: " + e.getMessage());
                errorMessage.postValue("Ошибка сети: " + e.getMessage());
                isLoading.postValue(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        String countStr = response.header("content-range");
                        if (countStr != null) {
                            String[] parts = countStr.split("/");
                            if (parts.length == 2) {
                                int count = Integer.parseInt(parts[1]);
                                callback.onResult(count);
                                return;
                            }
                        }
                    }
                    callback.onResult(0);
                } catch (Exception e) {
                    callback.onResult(0);
                }
            }
        });
    }

    interface CountCallback {
        void onResult(int count);
    }
}