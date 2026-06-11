package com.crossfitarmyjym.app.ui.trainer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.AttendanceEntry;
import com.crossfitarmyjym.app.data.repository.AttendanceRepository;

import java.util.ArrayList;
import java.util.List;

public class AttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;
    private final MutableLiveData<List<AttendanceEntry>> attendanceList =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> saveStatus = new MutableLiveData<>();
    private String classId;

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        repository = AttendanceRepository.getInstance(application);
    }

    public LiveData<List<AttendanceEntry>> getAttendanceList() {
        return attendanceList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSaveStatus() {
        return saveStatus;
    }

    public void loadAttendance(String selectedClassId) {
        classId = selectedClassId;
        isLoading.setValue(true);
        errorMessage.setValue(null);
        repository.getClassAttendance(selectedClassId,
                new AttendanceRepository.AttendanceCallback() {
                    @Override
                    public void onSuccess(List<AttendanceEntry> entries) {
                        attendanceList.setValue(entries);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(@NonNull String errorMessageValue) {
                        errorMessage.setValue(errorMessageValue);
                        isLoading.setValue(false);
                    }
                });
    }

    public void setAttended(String userId, boolean attended) {
        List<AttendanceEntry> entries = attendanceList.getValue();
        if (entries == null) {
            return;
        }
        for (AttendanceEntry entry : entries) {
            if (entry.getUserId().equals(userId)) {
                entry.setAttended(attended);
                break;
            }
        }
    }

    public void saveAttendance() {
        List<AttendanceEntry> entries = attendanceList.getValue();
        if (classId == null || entries == null) {
            errorMessage.setValue("Занятие не выбрано");
            return;
        }
        isLoading.setValue(true);
        saveStatus.setValue(null);
        repository.saveClassAttendance(classId, entries,
                new AttendanceRepository.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        saveStatus.setValue("Посещаемость сохранена");
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(@NonNull String errorMessageValue) {
                        errorMessage.setValue(errorMessageValue);
                        isLoading.setValue(false);
                    }
                });
    }
}
