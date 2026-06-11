package com.crossfitarmyjym.app.ui.client;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.repository.ClassRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel для главной страницы клиента.
 * Отображает WOD дня и ближайшее занятие.
 */
public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";

    private final WodRepository wodRepository;
    private final ClassRepository classRepository;

    private final MutableLiveData<Wod> todaysWod = new MutableLiveData<>();
    private final MutableLiveData<GymClass> nextClass = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        wodRepository = WodRepository.getInstance(application);
        classRepository = ClassRepository.getInstance(application);
    }

    public LiveData<Wod> getTodaysWod() {
        return todaysWod;
    }

    public LiveData<GymClass> getNextClass() {
        return nextClass;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Загрузить данные для главной страницы.
     */
    public void loadHomeData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        loadTodaysWod();
        loadNextClass();
    }

    private void loadTodaysWod() {
        wodRepository.getTodaysWod(new WodRepository.WodCallback() {
            @Override
            public void onSuccess(Wod wod) {
                Log.d(TAG, "WOD loaded: " + wod.getName());
                todaysWod.setValue(wod);
                checkLoadingComplete();
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading WOD: " + error);
                if (errorMessage.getValue() == null) {
                    errorMessage.setValue(error);
                }
                checkLoadingComplete();
            }
        });
    }

    private void loadNextClass() {
        String today = getTodayDateString();
        classRepository.getClassesFromDate(today, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> classes) {
                if (classes != null && !classes.isEmpty()) {
                    nextClass.setValue(classes.get(0));
                }
                checkLoadingComplete();
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading next class: " + error);
                checkLoadingComplete();
            }
        });
    }

    private void checkLoadingComplete() {
        // Если оба запроса завершены, скрываем загрузку
        if (todaysWod.getValue() != null || nextClass.getValue() != null || errorMessage.getValue() != null) {
            isLoading.setValue(false);
        }
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}