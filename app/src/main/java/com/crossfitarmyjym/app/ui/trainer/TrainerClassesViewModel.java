package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.crossfitarmyjym.app.data.repository.ClassRepository;

import java.util.List;

/**
 * ViewModel для списка занятий тренера.
 */
public class TrainerClassesViewModel extends AndroidViewModel {

    private static final String TAG = "TrainerClassesVM";

    private final ClassRepository classRepository;
    private final PreferencesManager prefsManager;

    private final MutableLiveData<List<GymClass>> classes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TrainerClassesViewModel(@NonNull Application application) {
        super(application);
        classRepository = ClassRepository.getInstance(application);
        prefsManager = PreferencesManager.getInstance();
    }

    public LiveData<List<GymClass>> getClasses() { return classes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadMyClasses() {
        String trainerId = prefsManager.getUserId();
        if (trainerId == null) {
            errorMessage.setValue("Тренер не авторизован");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        classRepository.getClassesByTrainer(trainerId, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> gymClasses) {
                Log.d(TAG, "Loaded " + gymClasses.size() + " classes");
                classes.setValue(gymClasses);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error: " + error);
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }
}