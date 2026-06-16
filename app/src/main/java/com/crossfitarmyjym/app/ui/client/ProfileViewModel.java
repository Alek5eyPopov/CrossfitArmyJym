package com.crossfitarmyjym.app.ui.client;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.PersonalRecordRequest;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.repository.ResultRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfileViewModel extends AndroidViewModel {

    private final WodRepository wodRepository;
    private final ResultRepository resultRepository = new ResultRepository();
    private final MutableLiveData<List<Exercise>> exercises = new MutableLiveData<>();
    private final MutableLiveData<List<PersonalRecord>> personalRecordBests = new MutableLiveData<>();
    private final MutableLiveData<List<PersonalRecord>> personalRecordHistory = new MutableLiveData<>();
    private final MutableLiveData<List<Result>> wodResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        wodRepository = WodRepository.getInstance(application);
    }

    public LiveData<List<Exercise>> getExercises() { return exercises; }
    public LiveData<List<PersonalRecord>> getPersonalRecordBests() { return personalRecordBests; }
    public LiveData<List<PersonalRecord>> getPersonalRecordHistory() { return personalRecordHistory; }
    public LiveData<List<Result>> getWodResults() { return wodResults; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadProgress() {
        loading.setValue(true);
        loadExercises();
        loadPersonalRecords();
        loadWodResults();
    }

    public void submitPersonalRecord(String exerciseId, Double value, String resultText,
                                     String unit, String achievedAt, String notes) {
        loading.setValue(true);
        wodRepository.submitPersonalRecord(new PersonalRecordRequest(
                exerciseId, value, resultText, unit, achievedAt, notes
        ), new WodRepository.PersonalRecordCallback() {
            @Override
            public void onSuccess(PersonalRecord record) {
                message.postValue("PR сохранен");
                loadPersonalRecords();
            }

            @Override
            public void onError(@NonNull String error) {
                loading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    private void loadExercises() {
        wodRepository.getExercises(new WodRepository.ExerciseListCallback() {
            @Override
            public void onSuccess(List<Exercise> value) {
                exercises.postValue(value);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });
    }

    private void loadPersonalRecords() {
        wodRepository.getMyPersonalRecords(new WodRepository.PersonalRecordListCallback() {
            @Override
            public void onSuccess(List<PersonalRecord> records) {
                personalRecordHistory.postValue(records);
                personalRecordBests.postValue(calculateBestRecords(records));
                loading.postValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                loading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    private List<PersonalRecord> calculateBestRecords(List<PersonalRecord> records) {
        Map<String, PersonalRecord> bestByExercise = new LinkedHashMap<>();
        if (records == null) {
            return new ArrayList<>();
        }

        for (PersonalRecord record : records) {
            String exerciseKey = record.getExerciseId();
            if (exerciseKey == null || exerciseKey.trim().isEmpty()) {
                exerciseKey = record.getExerciseName();
            }
            if (exerciseKey == null || exerciseKey.trim().isEmpty()) {
                continue;
            }

            PersonalRecord current = bestByExercise.get(exerciseKey);
            if (current == null || isBetter(record, current)) {
                bestByExercise.put(exerciseKey, record);
            }
        }
        return new ArrayList<>(bestByExercise.values());
    }

    private boolean isBetter(PersonalRecord candidate, PersonalRecord current) {
        if (candidate.getResultValue() == null || current.getResultValue() == null) {
            return false;
        }

        String direction = candidate.getExercise() != null
                ? candidate.getExercise().getPrBetterDirection()
                : null;
        if ("min".equalsIgnoreCase(direction)) {
            return candidate.getResultValue() < current.getResultValue();
        }
        return candidate.getResultValue() > current.getResultValue();
    }

    private void loadWodResults() {
        resultRepository.getMyResults(new ResultRepository.ResultsCallback() {
            @Override
            public void onSuccess(List<Result> results) {
                wodResults.postValue(results);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });
    }
}
