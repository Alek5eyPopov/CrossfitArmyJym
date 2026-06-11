package com.crossfitarmyjym.app.ui.client;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.LeaderboardEntry;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.repository.ClassRepository;
import com.crossfitarmyjym.app.data.repository.ResultRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private final WodRepository wodRepository;
    private final ClassRepository classRepository;
    private final ResultRepository resultRepository;
    private final MutableLiveData<Wod> todaysWod = new MutableLiveData<>();
    private final MutableLiveData<GymClass> nextClass = new MutableLiveData<>();
    private final MutableLiveData<List<LeaderboardEntry>> leaderboard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        wodRepository = WodRepository.getInstance(application);
        classRepository = ClassRepository.getInstance(application);
        resultRepository = new ResultRepository();
    }

    public LiveData<Wod> getTodaysWod() { return todaysWod; }
    public LiveData<GymClass> getNextClass() { return nextClass; }
    public LiveData<List<LeaderboardEntry>> getLeaderboard() { return leaderboard; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadHomeData() {
        isLoading.setValue(true);
        loadTodaysWod();
        loadNextClass();
    }

    public void submitResult(double score, String formattedScore) {
        Wod wod = todaysWod.getValue();
        if (wod == null) {
            errorMessage.setValue("WOD не загружен");
            return;
        }
        resultRepository.submit(wod.getId(), score, formattedScore,
                new ResultRepository.ResultCallback() {
                    @Override
                    public void onSuccess(Result result) {
                        message.postValue(result.isPr()
                                ? "Результат сохранён. Новый личный рекорд!"
                                : "Результат сохранён");
                    }

                    @Override
                    public void onError(@NonNull String error) {
                        errorMessage.postValue(error);
                    }
                });
    }

    public void loadLeaderboard() {
        Wod wod = todaysWod.getValue();
        if (wod == null) {
            errorMessage.setValue("WOD не загружен");
            return;
        }
        resultRepository.getLeaderboard(wod.getId(), new ResultRepository.LeaderboardCallback() {
            @Override
            public void onSuccess(List<LeaderboardEntry> entries) {
                leaderboard.postValue(entries);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });
    }

    private void loadTodaysWod() {
        wodRepository.getTodaysWod(new WodRepository.WodCallback() {
            @Override
            public void onSuccess(Wod wod) {
                todaysWod.postValue(wod);
                isLoading.postValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    private void loadNextClass() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        classRepository.getClassesFromDate(today, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(List<GymClass> classes) {
                if (classes != null && !classes.isEmpty()) nextClass.postValue(classes.get(0));
            }

            @Override
            public void onError(@NonNull String error) {
                // WOD remains useful even when no class is scheduled.
            }
        });
    }
}
