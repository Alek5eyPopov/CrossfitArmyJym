package com.crossfitarmyjym.app.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.repository.AdminRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminContentViewModel extends AndroidViewModel {

    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<List<GymClass>> classes = new MutableLiveData<>();
    private final MutableLiveData<List<Wod>> wods = new MutableLiveData<>();
    private final MutableLiveData<List<User>> trainers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminContentViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<List<GymClass>> getClasses() { return classes; }
    public LiveData<List<Wod>> getWods() { return wods; }
    public LiveData<List<User>> getTrainers() { return trainers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getError() { return error; }

    public void load() {
        loading.setValue(true);
        repository.getGroups(listCallback(groups));
        repository.getClasses(listCallback(classes));
        repository.getWods(listCallback(wods));
        repository.getUsers(new AdminRepository.ListCallback<User>() {
            @Override public void onSuccess(List<User> items) {
                List<User> trainerItems = new ArrayList<>();
                for (User user : items) {
                    if (user.isTrainer()) {
                        trainerItems.add(user);
                    }
                }
                trainers.postValue(trainerItems);
                loading.postValue(false);
            }
            @Override public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
    }

    public void saveGroup(Group group, String name, String trainerId, String schedule,
                          boolean active) {
        action(group == null
                ? callback -> repository.createGroup(name, trainerId, schedule, callback)
                : callback -> repository.updateGroup(
                        group.getId(), name, trainerId, schedule, active, callback
                ), "Группа сохранена");
    }

    public void deleteGroup(Group group) {
        action(callback -> repository.deleteGroup(group.getId(), callback), "Группа удалена");
    }

    public void saveClass(GymClass gymClass, Map<String, Object> fields) {
        action(gymClass == null
                ? callback -> repository.createClass(fields, callback)
                : callback -> repository.updateClass(gymClass.getId(), fields, callback),
                "Занятие сохранено");
    }

    public void deleteClass(GymClass gymClass) {
        action(callback -> repository.deleteClass(gymClass.getId(), callback), "Занятие удалено");
    }

    public void saveWod(Wod wod, Map<String, Object> fields) {
        action(callback -> repository.updateWod(wod.getId(), fields, callback), "WOD обновлён");
    }

    public void deleteWod(Wod wod) {
        action(callback -> repository.deleteWod(wod.getId(), callback), "WOD удалён");
    }

    private <T> AdminRepository.ListCallback<T> listCallback(MutableLiveData<List<T>> target) {
        return new AdminRepository.ListCallback<T>() {
            @Override public void onSuccess(List<T> items) { target.postValue(items); }
            @Override public void onError(@NonNull String value) { error.postValue(value); }
        };
    }

    private void action(Action action, String successMessage) {
        loading.setValue(true);
        action.run(new AdminRepository.ActionCallback() {
            @Override public void onSuccess() {
                message.postValue(successMessage);
                load();
            }
            @Override public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
    }

    private interface Action {
        void run(AdminRepository.ActionCallback callback);
    }
}
