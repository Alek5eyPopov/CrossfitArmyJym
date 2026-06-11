package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodCompositionRequest;
import com.crossfitarmyjym.app.data.model.WodExerciseInput;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.util.List;

public class WodEditorViewModel extends AndroidViewModel {

    private final WodRepository repository;
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<List<Exercise>> exercises = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public WodEditorViewModel(@NonNull Application application) {
        super(application);
        repository = WodRepository.getInstance(application);
    }

    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<List<Exercise>> getExercises() { return exercises; }
    public LiveData<Boolean> getIsSaving() { return isSaving; }
    public LiveData<String> getSaveResult() { return saveResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadReferenceData() {
        repository.getTrainerGroups(new WodRepository.GroupListCallback() {
            @Override
            public void onSuccess(List<Group> value) {
                groups.postValue(value);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });
        repository.getExercises(new WodRepository.ExerciseListCallback() {
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

    public void createWod(String name, String format, String groupId, String date,
                          int timeCapSeconds, String notes,
                          List<WodExerciseInput> selectedExercises) {
        if (selectedExercises == null || selectedExercises.isEmpty()) {
            errorMessage.setValue("Добавьте хотя бы одно упражнение");
            return;
        }
        isSaving.setValue(true);
        repository.createWod(new WodCompositionRequest(
                name, format, groupId, date, timeCapSeconds, notes, selectedExercises
        ), new WodRepository.WodCallback() {
            @Override
            public void onSuccess(Wod wod) {
                isSaving.postValue(false);
                saveResult.postValue("WOD создан");
            }

            @Override
            public void onError(@NonNull String error) {
                isSaving.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}
