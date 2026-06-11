package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ViewModel для управления WOD (админ-панель).
 * Позволяет просматривать, редактировать и удалять любые WOD.
 */
public class AdminWodViewModel extends AndroidViewModel {

    private static final String TAG = "AdminWodVM";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<List<Wod>> wods = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();

    public AdminWodViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<List<Wod>> getWods() { return wods; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getActionStatus() { return actionStatus; }

    /**
     * Загрузить все WOD.
     */
    public void loadAllWods() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "wods?order=scheduled_date.desc";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("apikey", apiKey)
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
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONArray arr = new JSONArray(json);
                        List<Wod> list = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Wod wod = new Wod();
                            list.add(wod);
                        }
                        wods.postValue(list);
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
     * Удалить WOD по ID.
     */
    public void deleteWod(String wodId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "wods?id=eq." + wodId;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("apikey", apiKey)
                .delete()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                errorMessage.postValue("Ошибка сети: " + e.getMessage());
                isLoading.postValue(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    actionStatus.postValue("WOD удален");
                    loadAllWods();
                } else {
                    errorMessage.postValue("Ошибка удаления: " + response.code());
                }
                isLoading.postValue(false);
            }
        });
    }
}