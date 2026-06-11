package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ClientsViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> clients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ClientsViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
    }

    public LiveData<List<User>> getClients() {
        return clients;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadClients() {
        isLoading.setValue(true);
        userRepository.getAllUsers(new UserRepository.UserListCallback() {
            @Override
            public void onSuccess(@NonNull List<User> users) {
                List<User> athletes = new ArrayList<>();
                for (User user : users) {
                    if (user.isAthlete() && user.isActive()) {
                        athletes.add(user);
                    }
                }
                clients.setValue(athletes);
                isLoading.setValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }
}
