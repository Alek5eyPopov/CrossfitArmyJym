package com.crossfitarmyjym.app.data.repository;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.AdminApi;
import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.model.AdminStats;
import com.crossfitarmyjym.app.data.model.Attendance;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private final AdminApi api = ApiClient.getAdminApi();

    public interface ListCallback<T> {
        void onSuccess(List<T> items);
        void onError(@NonNull String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(@NonNull String error);
    }

    public interface StatsCallback {
        void onSuccess(AdminStats stats);
        void onError(@NonNull String error);
    }

    public void getUsers(ListCallback<User> callback) {
        enqueueList(api.getUsers("created_at.desc"), callback, "пользователей");
    }

    public void updateUser(String id, String role, String groupId, boolean active,
                           ActionCallback callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("role", role);
        fields.put("group_id", groupId);
        fields.put("is_active", active);
        enqueueMutation(api.updateUser("eq." + id, "*", fields), callback);
    }

    public void getGroups(ListCallback<Group> callback) {
        enqueueList(api.getGroups("name.asc"), callback, "группы");
    }

    public void createGroup(String name, String trainerId, String schedule,
                            ActionCallback callback) {
        enqueueMutation(api.createGroup("*", groupFields(name, trainerId, schedule, true)), callback);
    }

    public void updateGroup(String id, String name, String trainerId, String schedule,
                            boolean active, ActionCallback callback) {
        enqueueMutation(api.updateGroup(
                "eq." + id, "*", groupFields(name, trainerId, schedule, active)
        ), callback);
    }

    public void deleteGroup(String id, ActionCallback callback) {
        enqueueVoid(api.deleteGroup("eq." + id), callback);
    }

    public void getClasses(ListCallback<GymClass> callback) {
        enqueueList(api.getClasses("scheduled_start.desc"), callback, "занятия");
    }

    public void createClass(Map<String, Object> fields, ActionCallback callback) {
        enqueueMutation(api.createClass("*", fields), callback);
    }

    public void updateClass(String id, Map<String, Object> fields, ActionCallback callback) {
        enqueueMutation(api.updateClass("eq." + id, "*", fields), callback);
    }

    public void deleteClass(String id, ActionCallback callback) {
        enqueueVoid(api.deleteClass("eq." + id), callback);
    }

    public void getWods(ListCallback<Wod> callback) {
        enqueueList(api.getWods("scheduled_date.desc"), callback, "WOD");
    }

    public void updateWod(String id, Map<String, Object> fields, ActionCallback callback) {
        enqueueMutation(api.updateWod("eq." + id, "*", fields), callback);
    }

    public void deleteWod(String id, ActionCallback callback) {
        enqueueVoid(api.deleteWod("eq." + id), callback);
    }

    public void getStats(StatsCallback callback) {
        StatsAccumulator accumulator = new StatsAccumulator(callback);
        enqueueList(api.getUsers("created_at.desc"), accumulator.users(), "пользователей");
        enqueueList(api.getGroups("name.asc"), accumulator.groups(), "группы");
        enqueueList(api.getClasses("scheduled_start.desc"), accumulator.classes(), "занятия");
        enqueueList(api.getBookings("eq.confirmed"), accumulator.bookings(), "записи");
        enqueueList(api.getAttendance(), accumulator.attendance(), "посещаемость");
        enqueueList(api.getResults(), accumulator.results(), "результаты");
    }

    public static Map<String, Object> classFields(String groupId, String trainerId,
                                                   String start, String end, int capacity,
                                                   String location, String status) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("group_id", groupId);
        fields.put("trainer_id", trainerId);
        fields.put("scheduled_start", start);
        fields.put("scheduled_end", end);
        fields.put("max_capacity", Math.max(capacity, 1));
        fields.put("location", location);
        fields.put("status", status);
        return fields;
    }

    public static Map<String, Object> wodFields(String name, String date, String notes) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("scheduled_date", date);
        fields.put("notes", notes);
        return fields;
    }

    private Map<String, Object> groupFields(String name, String trainerId, String schedule,
                                            boolean active) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("trainer_id", trainerId);
        fields.put("schedule", schedule);
        fields.put("is_active", active);
        return fields;
    }

    private <T> void enqueueList(Call<List<T>> call, ListCallback<T> callback, String label) {
        call.enqueue(new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Не удалось загрузить " + label + ": HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    private <T> void enqueueMutation(Call<List<T>> call, ActionCallback callback) {
        call.enqueue(new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Операция отклонена: HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    private void enqueueVoid(Call<Void> call, ActionCallback callback) {
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Операция отклонена: HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    private static final class StatsAccumulator {
        private final AtomicInteger remaining = new AtomicInteger(6);
        private final StatsCallback callback;
        private int users;
        private int activeUsers;
        private int groups;
        private int classes;
        private int bookings;
        private int attended;
        private int results;
        private boolean failed;

        StatsAccumulator(StatsCallback callback) { this.callback = callback; }

        ListCallback<User> users() {
            return listener(items -> {
                users = items.size();
                activeUsers = (int) items.stream().filter(User::isActive).count();
            });
        }

        ListCallback<Group> groups() { return listener(items -> groups = items.size()); }
        ListCallback<GymClass> classes() { return listener(items -> classes = items.size()); }
        ListCallback<Booking> bookings() { return listener(items -> bookings = items.size()); }
        ListCallback<Attendance> attendance() {
            return listener(items -> attended = (int) items.stream()
                    .filter(Attendance::isAttended).count());
        }
        ListCallback<Result> results() { return listener(items -> results = items.size()); }

        private <T> ListCallback<T> listener(java.util.function.Consumer<List<T>> consumer) {
            return new ListCallback<T>() {
                @Override
                public void onSuccess(List<T> items) {
                    consumer.accept(items);
                    finish();
                }

                @Override
                public void onError(@NonNull String error) {
                    if (!failed) {
                        failed = true;
                        callback.onError(error);
                    }
                }
            };
        }

        private void finish() {
            if (remaining.decrementAndGet() == 0 && !failed) {
                callback.onSuccess(new AdminStats(
                        users, activeUsers, groups, classes, bookings, attended, results
                ));
            }
        }
    }
}
