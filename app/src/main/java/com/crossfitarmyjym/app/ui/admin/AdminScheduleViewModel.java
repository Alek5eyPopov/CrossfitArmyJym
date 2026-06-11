package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ViewModel для управления расписанием занятий (админ-панель).
 * CRUD операции над таблицей classes.
 */
public class AdminScheduleViewModel extends AndroidViewModel {

    private static final String TAG = "AdminScheduleVM";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();

    public AdminScheduleViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getActionStatus() { return actionStatus; }

    /**
     * Создать новое занятие.
     */
    public void createClass(String groupId, String trainerId, String startTime, String endTime,
                            int maxCapacity, String location) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "classes";

        try {
            JSONObject body = new JSONObject();
            if (groupId != null) body.put("group_id", groupId);
            if (trainerId != null) body.put("trainer_id", trainerId);
            body.put("scheduled_start", startTime);
            body.put("scheduled_end", endTime);
            body.put("max_capacity", maxCapacity);
            body.put("location", location != null ? location : "Main Box");
            body.put("current_bookings", 0);
            body.put("status", "scheduled");

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", apiKey)
                    .header("Prefer", "return=representation")
                    .post(RequestBody.create(body.toString(), JSON))
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
                    if (response.isSuccessful()) {
                        actionStatus.postValue("Занятие создано");
                    } else {
                        errorMessage.postValue("Ошибка: " + response.code());
                    }
                    isLoading.postValue(false);
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("Ошибка: " + e.getMessage());
            isLoading.setValue(false);
        }
    }
}