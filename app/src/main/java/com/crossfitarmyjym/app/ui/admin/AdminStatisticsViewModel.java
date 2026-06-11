package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.AdminStats;
import com.crossfitarmyjym.app.data.repository.AdminRepository;

public class AdminStatisticsViewModel extends AndroidViewModel {

    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<AdminStats> stats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminStatisticsViewModel(@NonNull Application application) { super(application); }

    public LiveData<AdminStats> getStats() { return stats; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void load() {
        loading.setValue(true);
        repository.getStats(new AdminRepository.StatsCallback() {
            @Override public void onSuccess(AdminStats value) {
                stats.postValue(value);
                loading.postValue(false);
            }
            @Override public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
    }
}
