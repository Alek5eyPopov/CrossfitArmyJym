package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.repository.AdminRepository;
import com.crossfitarmyjym.app.data.repository.ResultRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersViewModel extends AndroidViewModel {

    private final AdminRepository repository = new AdminRepository();
    private final WodRepository wodRepository;
    private final ResultRepository resultRepository = new ResultRepository();
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private final MutableLiveData<List<PersonalRecord>> selectedUserBests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PersonalRecord>> selectedUserPrHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Result>> selectedUserWodResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> progressLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminUsersViewModel(@NonNull Application application) {
        super(application);
        wodRepository = WodRepository.getInstance(application);
    }

    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<User> getSelectedUser() { return selectedUser; }
    public LiveData<List<PersonalRecord>> getSelectedUserBests() { return selectedUserBests; }
    public LiveData<List<PersonalRecord>> getSelectedUserPrHistory() { return selectedUserPrHistory; }
    public LiveData<List<Result>> getSelectedUserWodResults() { return selectedUserWodResults; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getProgressLoading() { return progressLoading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getError() { return error; }

    public void load() {
        loading.setValue(true);
        repository.getUsers(new AdminRepository.ListCallback<User>() {
            @Override public void onSuccess(List<User> items) {
                users.postValue(items);
                loading.postValue(false);
            }
            @Override public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
        repository.getGroups(new AdminRepository.ListCallback<Group>() {
            @Override public void onSuccess(List<Group> items) { groups.postValue(items); }
            @Override public void onError(@NonNull String value) { error.postValue(value); }
        });
    }

    public void update(User user, String role, String groupId, boolean active) {
        if (user.getId() == null) return;
        loading.setValue(true);
        repository.updateUser(user.getId().toString(), role, groupId, active,
                new AdminRepository.ActionCallback() {
                    @Override public void onSuccess() {
                        message.postValue("Профиль обновлён");
                        load();
                    }
                    @Override public void onError(@NonNull String value) {
                        error.postValue(value);
                        loading.postValue(false);
                    }
                });
    }

    public void loadUserProgress(@NonNull User user) {
        selectedUser.setValue(user);
        selectedUserBests.setValue(new ArrayList<>());
        selectedUserPrHistory.setValue(new ArrayList<>());
        selectedUserWodResults.setValue(new ArrayList<>());
        progressLoading.setValue(true);

        String userId = user.getId() != null ? user.getId().toString() : null;
        wodRepository.getPersonalRecordBestsForUser(userId, new WodRepository.PersonalRecordListCallback() {
            @Override public void onSuccess(List<PersonalRecord> records) {
                selectedUserBests.postValue(records);
            }

            @Override public void onError(@NonNull String value) {
                error.postValue(value);
            }
        });

        wodRepository.getPersonalRecordsForUser(userId, new WodRepository.PersonalRecordListCallback() {
            @Override public void onSuccess(List<PersonalRecord> records) {
                selectedUserPrHistory.postValue(records);
            }

            @Override public void onError(@NonNull String value) {
                error.postValue(value);
            }
        });

        resultRepository.getResultsForUser(userId, new ResultRepository.ResultsCallback() {
            @Override public void onSuccess(List<Result> results) {
                selectedUserWodResults.postValue(results);
                progressLoading.postValue(false);
            }

            @Override public void onError(@NonNull String value) {
                progressLoading.postValue(false);
                error.postValue(value);
            }
        });
    }
}
