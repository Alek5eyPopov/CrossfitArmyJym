package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodTaskCompositionRequest;
import com.crossfitarmyjym.app.data.model.WodTaskInput;
import com.crossfitarmyjym.app.data.repository.AdminRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.util.List;

public class WodEditorViewModel extends AndroidViewModel {

    private final WodRepository repository;
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<List<Exercise>> exercises = new MutableLiveData<>();
    private final MutableLiveData<List<LoadType>> loadTypes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public WodEditorViewModel(@NonNull Application application) {
        super(application);
        repository = WodRepository.getInstance(application);
    }

    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<List<Exercise>> getExercises() { return exercises; }
    public LiveData<List<LoadType>> getLoadTypes() { return loadTypes; }
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
        repository.getLoadTypes(new WodRepository.LoadTypeListCallback() {
            @Override
            public void onSuccess(List<LoadType> value) {
                loadTypes.postValue(value);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });
    }

    public void createWod(String name, String format, String groupId, String date,
                          int timeCapSeconds, String notes,
                          List<WodTaskInput> selectedTasks) {
        if (selectedTasks == null || selectedTasks.isEmpty()) {
            errorMessage.setValue("Добавьте хотя бы одно задание");
            return;
        }
        isSaving.setValue(true);
        repository.createWodWithTasks(new WodTaskCompositionRequest(
                name, format, groupId, date, timeCapSeconds, notes, selectedTasks
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

    public void createExercise(String name, String category, String description,
                               String unitType, String prUnit, String prDirection) {
        repository.createExercise(AdminRepository.exerciseFields(
                name, category, description, unitType, prUnit, prDirection, true
        ), referenceCallback("Упражнение создано"));
    }

    public void createLoadType(String code, String name, String description) {
        repository.createLoadType(AdminRepository.loadTypeFields(
                code, name, description, true
        ), referenceCallback("Тип нагрузки создан"));
    }

    public void createTrainingTask(String title, String exerciseId, String loadTypeId,
                                   String rxLoad, String optionalExerciseId,
                                   String optionalLoadTypeId, String optionalLoad,
                                   String notes) {
        repository.createTrainingTask(AdminRepository.trainingTaskFields(
                title, exerciseId, loadTypeId, rxLoad, optionalExerciseId,
                optionalLoadTypeId, optionalLoad, notes, true
        ), referenceCallback("Шаблон задания создан"));
    }

    private WodRepository.ReferenceActionCallback referenceCallback(String successMessage) {
        return new WodRepository.ReferenceActionCallback() {
            @Override
            public void onSuccess() {
                saveResult.postValue(successMessage);
                loadReferenceData();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                errorMessage(errorMessage);
            }
        };
    }

    private void errorMessage(String value) {
        errorMessage.postValue(value);
    }
}
