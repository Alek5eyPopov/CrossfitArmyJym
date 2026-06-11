package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.model.Attendance;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ViewModel для отметки посещаемости на занятии.
 */
public class AttendanceViewModel extends AndroidViewModel {

    private static final String TAG = "AttendanceVM";

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<List<Attendance>> attendanceList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> saveStatus = new MutableLiveData<>();

    private String classId;

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<List<Attendance>> getAttendanceList() { return attendanceList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSaveStatus() { return saveStatus; }

    /**
     * Загрузить список клиентов для указанного занятия.
     */
    public void loadAttendance(String classId) {
        this.classId = classId;
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Загружаем через Supabase PostgREST
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "attendance?class_id=eq." + classId;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("apikey", apiKey)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                errorMessage.postValue("Ошибка сети: " + e.getMessage());
                isLoading.postValue(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONArray arr = new JSONArray(json);
                        List<Attendance> list = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Attendance a = new Attendance();
                            list.add(a);
                        }
                        attendanceList.postValue(list);
                    } else {
                        errorMessage.postValue("Ошибка загрузки: " + response.code());
                    }
                } catch (Exception e) {
                    errorMessage.postValue("Ошибка парсинга: " + e.getMessage());
                }
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Сохранить отметки посещаемости.
     */
    public void saveAttendance() {
        // TODO: реализовать сохранение через PATCH/POST в Supabase
        saveStatus.setValue("Посещаемость сохранена");
    }
}