package com.crossfitarmyjym.app.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.WodApi;
import com.crossfitarmyjym.app.data.local.AppDatabase;
import com.crossfitarmyjym.app.data.local.entity.WodEntity;
import com.crossfitarmyjym.app.data.local.entity.dao.WodDao;
import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.PersonalRecordRequest;
import com.crossfitarmyjym.app.data.model.TrainingTask;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodCompositionRequest;
import com.crossfitarmyjym.app.data.model.WodTaskCompositionRequest;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WodRepository {

    private static final String WOD_SELECT =
            "*,wod_exercises(*,exercises(*)),wod_tasks(*)";
    private static final String TRAINING_TASK_SELECT =
            "*,rx_exercise:exercises!training_tasks_rx_exercise_id_fkey(*)," +
                    "load_type:load_types!training_tasks_load_type_id_fkey(*)," +
                    "optional_exercise:exercises!training_tasks_optional_exercise_id_fkey(*)," +
                    "optional_load_type:load_types!training_tasks_optional_load_type_id_fkey(*)";
    private static final String PERSONAL_RECORD_SELECT = "*,exercises(*)";
    private static WodRepository instance;

    private final WodApi api;
    private final WodDao dao;
    private final PreferencesManager preferences;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public interface WodCallback {
        void onSuccess(Wod wod);
        void onError(@NonNull String errorMessage);
    }

    public interface WodListCallback {
        void onSuccess(List<Wod> wods);
        void onError(@NonNull String errorMessage);
    }

    public interface GroupListCallback {
        void onSuccess(List<Group> groups);
        void onError(@NonNull String errorMessage);
    }

    public interface ExerciseListCallback {
        void onSuccess(List<Exercise> exercises);
        void onError(@NonNull String errorMessage);
    }

    public interface LoadTypeListCallback {
        void onSuccess(List<LoadType> loadTypes);
        void onError(@NonNull String errorMessage);
    }

    public interface TrainingTaskListCallback {
        void onSuccess(List<TrainingTask> tasks);
        void onError(@NonNull String errorMessage);
    }

    public interface PersonalRecordCallback {
        void onSuccess(PersonalRecord record);
        void onError(@NonNull String errorMessage);
    }

    public interface PersonalRecordListCallback {
        void onSuccess(List<PersonalRecord> records);
        void onError(@NonNull String errorMessage);
    }

    private WodRepository(Context context) {
        api = ApiClient.getWodApi();
        dao = AppDatabase.getInstance(context).wodDao();
        preferences = PreferencesManager.getInstance();
    }

    public static synchronized WodRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WodRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getTodaysWod(WodCallback callback) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        api.getWodByDate("eq." + today, 1, WOD_SELECT).enqueue(wodResponse(callback, today));
    }

    public void getWodById(String wodId, WodCallback callback) {
        api.getWodById("eq." + wodId, WOD_SELECT).enqueue(wodResponse(callback, null));
    }

    public void getAllWods(WodListCallback callback) {
        api.getWods(50, WOD_SELECT).enqueue(new Callback<List<Wod>>() {
            @Override
            public void onResponse(Call<List<Wod>> call, Response<List<Wod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheWods(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось загрузить WOD: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Wod>> call, Throwable throwable) {
                databaseExecutor.execute(() -> callback.onSuccess(entitiesToModels(dao.getAllWods())));
            }
        });
    }

    public void getTrainerGroups(GroupListCallback callback) {
        String trainerId = preferences.getUserId();
        if (trainerId == null) {
            callback.onError("Тренер не авторизован");
            return;
        }
        String trainerFilter = "admin".equals(preferences.getUserRole())
                ? null : "eq." + trainerId;
        api.getTrainerGroups(trainerFilter, "eq.true", "name.asc")
                .enqueue(new Callback<List<Group>>() {
                    @Override
                    public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Не удалось загрузить группы: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Group>> call, Throwable throwable) {
                        callback.onError("Ошибка сети: " + throwable.getMessage());
                    }
                });
    }

    public void getExercises(ExerciseListCallback callback) {
        api.getExercises("eq.true", "name.asc").enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось загрузить упражнения: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void getLoadTypes(LoadTypeListCallback callback) {
        api.getLoadTypes("eq.true", "name.asc").enqueue(new Callback<List<LoadType>>() {
            @Override
            public void onResponse(Call<List<LoadType>> call, Response<List<LoadType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось загрузить типы нагрузки: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LoadType>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void getTrainingTasks(TrainingTaskListCallback callback) {
        api.getTrainingTasks("eq.true", "title.asc", TRAINING_TASK_SELECT)
                .enqueue(new Callback<List<TrainingTask>>() {
                    @Override
                    public void onResponse(Call<List<TrainingTask>> call,
                                           Response<List<TrainingTask>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Не удалось загрузить задания: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TrainingTask>> call, Throwable throwable) {
                        callback.onError("Ошибка сети: " + throwable.getMessage());
                    }
                });
    }

    public void createWod(WodCompositionRequest request, WodCallback callback) {
        api.createWod(request).enqueue(new Callback<Wod>() {
            @Override
            public void onResponse(Call<Wod> call, Response<Wod> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheWod(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось создать WOD: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Wod> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void createWodWithTasks(WodTaskCompositionRequest request, WodCallback callback) {
        api.createWodWithTasks(request).enqueue(new Callback<Wod>() {
            @Override
            public void onResponse(Call<Wod> call, Response<Wod> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheWod(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось создать WOD: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Wod> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void submitPersonalRecord(PersonalRecordRequest request,
                                     PersonalRecordCallback callback) {
        api.submitPersonalRecord(request).enqueue(new Callback<PersonalRecord>() {
            @Override
            public void onResponse(Call<PersonalRecord> call,
                                   Response<PersonalRecord> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось сохранить PR: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PersonalRecord> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void getMyPersonalRecords(PersonalRecordListCallback callback) {
        String userId = preferences.getUserId();
        if (userId == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }
        api.getPersonalRecords("eq." + userId, "achieved_at.desc", PERSONAL_RECORD_SELECT)
                .enqueue(personalRecordListResponse(callback,
                        "Не удалось загрузить историю PR"));
    }

    public void getMyPersonalRecordBests(PersonalRecordListCallback callback) {
        String userId = preferences.getUserId();
        if (userId == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }
        api.getPersonalRecordBests("eq." + userId, "exercise_name.asc")
                .enqueue(personalRecordListResponse(callback,
                        "Не удалось загрузить лучшие PR"));
    }

    private Callback<List<PersonalRecord>> personalRecordListResponse(
            PersonalRecordListCallback callback,
            String errorPrefix
    ) {
        return new Callback<List<PersonalRecord>>() {
            @Override
            public void onResponse(Call<List<PersonalRecord>> call,
                                   Response<List<PersonalRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorPrefix + ": HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PersonalRecord>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        };
    }

    private Callback<List<Wod>> wodResponse(WodCallback callback, String fallbackDate) {
        return new Callback<List<Wod>>() {
            @Override
            public void onResponse(Call<List<Wod>> call, Response<List<Wod>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wod wod = response.body().get(0);
                    cacheWod(wod);
                    callback.onSuccess(wod);
                } else {
                    loadFallback(callback, fallbackDate, "WOD не найден");
                }
            }

            @Override
            public void onFailure(Call<List<Wod>> call, Throwable throwable) {
                loadFallback(callback, fallbackDate, "Ошибка сети: " + throwable.getMessage());
            }
        };
    }

    private void loadFallback(WodCallback callback, String date, String error) {
        databaseExecutor.execute(() -> {
            WodEntity entity = date == null ? null : dao.getWodByDate(date);
            if (entity != null) {
                callback.onSuccess(entityToModel(entity));
            } else {
                callback.onError(error);
            }
        });
    }

    private void cacheWod(Wod wod) {
        if (wod == null || wod.getId() == null) return;
        databaseExecutor.execute(() -> dao.insert(modelToEntity(wod)));
    }

    private void cacheWods(List<Wod> wods) {
        databaseExecutor.execute(() -> {
            List<WodEntity> entities = new ArrayList<>();
            for (Wod wod : wods) {
                if (wod.getId() != null) entities.add(modelToEntity(wod));
            }
            if (!entities.isEmpty()) dao.insertAll(entities);
        });
    }

    private WodEntity modelToEntity(Wod wod) {
        WodEntity entity = new WodEntity(wod.getId());
        entity.setName(wod.getName());
        entity.setFormat(wod.getFormat());
        entity.setTrainerId(wod.getTrainerId());
        entity.setScheduledDate(wod.getScheduledDate());
        entity.setTimeCapSeconds(wod.getTimeCapSeconds());
        entity.setNotes(wod.getNotes());
        entity.setCreatedAt(wod.getCreatedAt());
        return entity;
    }

    private Wod entityToModel(WodEntity entity) {
        Wod wod = new Wod();
        wod.setId(entity.getId());
        wod.setName(entity.getName());
        wod.setFormat(entity.getFormat());
        wod.setTrainerId(entity.getTrainerId());
        wod.setScheduledDate(entity.getScheduledDate());
        wod.setTimeCapSeconds(entity.getTimeCapSeconds());
        wod.setNotes(entity.getNotes());
        wod.setCreatedAt(entity.getCreatedAt());
        return wod;
    }

    private List<Wod> entitiesToModels(List<WodEntity> entities) {
        List<Wod> result = new ArrayList<>();
        if (entities != null) {
            for (WodEntity entity : entities) result.add(entityToModel(entity));
        }
        return result;
    }
}
