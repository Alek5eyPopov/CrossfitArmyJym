package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.repository.ResultRepository;
import com.crossfitarmyjym.app.data.repository.UserRepository;
import com.crossfitarmyjym.app.data.repository.WodRepository;

import java.util.ArrayList;
import java.util.List;

public class ClientsViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final WodRepository wodRepository;
    private final ResultRepository resultRepository;
    private final MutableLiveData<List<User>> clients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<User> selectedClient = new MutableLiveData<>();
    private final MutableLiveData<List<PersonalRecord>> selectedClientBests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PersonalRecord>> selectedClientPrHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Result>> selectedClientWodResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> progressLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ClientsViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
        wodRepository = WodRepository.getInstance(application);
        resultRepository = new ResultRepository();
    }

    public LiveData<List<User>> getClients() {
        return clients;
    }

    public LiveData<User> getSelectedClient() {
        return selectedClient;
    }

    public LiveData<List<PersonalRecord>> getSelectedClientBests() {
        return selectedClientBests;
    }

    public LiveData<List<PersonalRecord>> getSelectedClientPrHistory() {
        return selectedClientPrHistory;
    }

    public LiveData<List<Result>> getSelectedClientWodResults() {
        return selectedClientWodResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getProgressLoading() {
        return progressLoading;
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

    public void loadClientProgress(@NonNull User client) {
        selectedClient.setValue(client);
        selectedClientBests.setValue(new ArrayList<>());
        selectedClientPrHistory.setValue(new ArrayList<>());
        selectedClientWodResults.setValue(new ArrayList<>());
        progressLoading.setValue(true);

        String userId = client.getId() != null ? client.getId().toString() : null;
        wodRepository.getPersonalRecordBestsForUser(userId, new WodRepository.PersonalRecordListCallback() {
            @Override
            public void onSuccess(List<PersonalRecord> records) {
                selectedClientBests.postValue(records);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });

        wodRepository.getPersonalRecordsForUser(userId, new WodRepository.PersonalRecordListCallback() {
            @Override
            public void onSuccess(List<PersonalRecord> records) {
                selectedClientPrHistory.postValue(records);
            }

            @Override
            public void onError(@NonNull String error) {
                errorMessage.postValue(error);
            }
        });

        resultRepository.getResultsForUser(userId, new ResultRepository.ResultsCallback() {
            @Override
            public void onSuccess(List<Result> results) {
                selectedClientWodResults.postValue(results);
                progressLoading.postValue(false);
            }

            @Override
            public void onError(@NonNull String error) {
                progressLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}
