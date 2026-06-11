package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.SupabaseConfig;
import com.crossfitarmyjym.app.data.model.Profile;
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
 * ViewModel для управления пользователями (админ-панель).
 */
public class AdminUsersViewModel extends AndroidViewModel {

    private static final String TAG = "AdminUsersVM";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final PreferencesManager prefsManager;
    private final OkHttpClient httpClient;

    private final MutableLiveData<List<Profile>> users = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();

    public AdminUsersViewModel(@NonNull Application application) {
        super(application);
        prefsManager = PreferencesManager.getInstance();
        httpClient = new OkHttpClient();
    }

    public LiveData<List<Profile>> getUsers() { return users; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getActionStatus() { return actionStatus; }

    /**
     * Загрузить список всех пользователей.
     */
    public void loadUsers() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "profiles?order=created_at.desc";

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
                        List<Profile> list = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Profile p = new Profile();
                            // TODO: добавить сеттеры в Profile модель или временно парсить без них
                            list.add(p);
                        }
                        users.postValue(list);
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
     * Заблокировать пользователя (is_active = false).
     */
    public void blockUser(String userId) {
        updateUserActiveStatus(userId, false);
    }

    /**
     * Разблокировать пользователя (is_active = true).
     */
    public void unblockUser(String userId) {
        updateUserActiveStatus(userId, true);
    }

    private void updateUserActiveStatus(String userId, boolean isActive) {
        String token = prefsManager.getAuthToken();
        String apiKey = SupabaseConfig.getApiKeyHeader();
        String url = SupabaseConfig.getSupabaseUrl() + "profiles?id=eq." + userId;

        try {
            JSONObject body = new JSONObject();
            body.put("is_active", isActive);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", apiKey)
                    .header("Prefer", "return=minimal")
                    .patch(RequestBody.create(body.toString(), JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    errorMessage.postValue("Ошибка сети: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        actionStatus.postValue(isActive ? "Пользователь разблокирован" : "Пользователь заблокирован");
                        loadUsers();
                    } else {
                        errorMessage.postValue("Ошибка: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("Ошибка: " + e.getMessage());
        }
    }
}