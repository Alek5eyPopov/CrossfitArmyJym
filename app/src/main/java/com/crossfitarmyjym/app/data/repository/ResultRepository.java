package com.crossfitarmyjym.app.data.repository;

import androidx.annotation.NonNull;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.WodApi;
import com.crossfitarmyjym.app.data.model.LeaderboardEntry;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.ResultRequest;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultRepository {

    private final WodApi api = ApiClient.getWodApi();

    public interface ResultCallback {
        void onSuccess(Result result);
        void onError(@NonNull String error);
    }

    public interface LeaderboardCallback {
        void onSuccess(List<LeaderboardEntry> entries);
        void onError(@NonNull String error);
    }

    public interface ResultsCallback {
        void onSuccess(List<Result> results);
        void onError(@NonNull String error);
    }

    public void submit(String wodId, double score, String formattedScore, ResultCallback callback) {
        api.submitResult(ResultRequest.submission(wodId, score, formattedScore))
                .enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Не удалось сохранить результат: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable throwable) {
                        callback.onError("Ошибка сети: " + throwable.getMessage());
                    }
                });
    }

    public void getLeaderboard(String wodId, LeaderboardCallback callback) {
        api.getLeaderboard(ResultRequest.leaderboard(wodId))
                .enqueue(new Callback<List<LeaderboardEntry>>() {
                    @Override
                    public void onResponse(Call<List<LeaderboardEntry>> call,
                                           Response<List<LeaderboardEntry>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Не удалось загрузить рейтинг: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LeaderboardEntry>> call, Throwable throwable) {
                        callback.onError("Ошибка сети: " + throwable.getMessage());
                    }
                });
    }

    public void getMyResults(ResultsCallback callback) {
        String userId = PreferencesManager.getInstance().getUserId();
        if (userId == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }
        api.getMyResults("eq." + userId, "completed_at.desc")
                .enqueue(new Callback<List<Result>>() {
                    @Override
                    public void onResponse(Call<List<Result>> call, Response<List<Result>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Не удалось загрузить результаты: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Result>> call, Throwable throwable) {
                        callback.onError("Ошибка сети: " + throwable.getMessage());
                    }
                });
    }
}
