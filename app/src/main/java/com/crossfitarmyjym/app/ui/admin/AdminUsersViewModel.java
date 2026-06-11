package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.repository.AdminRepository;

import java.util.List;

public class AdminUsersViewModel extends AndroidViewModel {

    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminUsersViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<Boolean> getLoading() { return loading; }
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
}
