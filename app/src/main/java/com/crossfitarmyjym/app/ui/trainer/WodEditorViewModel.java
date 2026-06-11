package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel для редактора WOD.
 * Позволяет тренеру создавать и редактировать тренировки дня.
 */
public class WodEditorViewModel extends AndroidViewModel {

    private static final String TAG = "WodEditorVM";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public WodEditorViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<Boolean> getIsSaving() { return isSaving; }
    public LiveData<String> getSaveResult() { return saveResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * Создать новый WOD.
     */
    public void createWod(String name, String format, String description, String notes) {
        String trainerId = prefsManager.getUserId();
        if (trainerId == null) {
            errorMessage.setValue("Тренер не авторизован");
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        isSaving.setValue(true);
        errorMessage.setValue(null);

        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("format", format);
            body.put("trainer_id", trainerId);
            body.put("scheduled_date", today);
            body.put("notes", notes != null ? notes : "");

            String token = prefsManager.getAuthToken();
            String apiKey = SupabaseConfig.getApiKeyHeader();
            String url = SupabaseConfig.getSupabaseUrl() + "wods";

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
                    isSaving.postValue(false);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        saveResult.postValue("WOD создан");
                    } else {
                        errorMessage.postValue("Ошибка: " + response.code());
                    }
                    isSaving.postValue(false);
                }
            });

        } catch (Exception e) {
            errorMessage.setValue("Ошибка: " + e.getMessage());
            isSaving.setValue(false);
        }
    }
}