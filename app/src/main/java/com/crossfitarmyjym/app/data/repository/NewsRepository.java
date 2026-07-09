package com.crossfitarmyjym.app.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crossfitarmyjym.app.data.api.ApiClient;
import com.crossfitarmyjym.app.data.api.NewsApi;
import com.crossfitarmyjym.app.data.model.NewsPost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsRepository {

    private static NewsRepository instance;

    private final NewsApi api;
    private final StorageRepository storageRepository;

    public interface NewsListCallback {
        void onSuccess(List<NewsPost> posts);
        void onError(@NonNull String error);
    }

    public interface NewsPostCallback {
        void onSuccess(NewsPost post);
        void onError(@NonNull String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(@NonNull String error);
    }

    private NewsRepository(Context context) {
        api = ApiClient.getNewsApi();
        storageRepository = new StorageRepository(context);
    }

    public static synchronized NewsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new NewsRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getPublishedNews(NewsListCallback callback) {
        enqueueList(api.getNews("eq." + NewsPost.STATUS_PUBLISHED,
                "published_at.desc.nullslast"), callback, "новости");
    }

    public void getNewsById(String id, NewsPostCallback callback) {
        api.getNewsById("eq." + id).enqueue(new Callback<List<NewsPost>>() {
            @Override
            public void onResponse(Call<List<NewsPost>> call, Response<List<NewsPost>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Новость не найдена");
                }
            }

            @Override
            public void onFailure(Call<List<NewsPost>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    public void getAdminNews(NewsListCallback callback) {
        enqueueList(api.getAdminNews("created_at.desc"), callback, "новости");
    }

    public void saveNews(@Nullable NewsPost existing, String title, String summary,
                         String body, String status, @Nullable Uri imageUri,
                         ActionCallback callback) {
        if (imageUri == null) {
            saveNewsFields(existing, fields(title, summary, body, status, null), callback);
            return;
        }
        storageRepository.uploadNewsImage(imageUri, new StorageRepository.UploadCallback() {
            @Override
            public void onSuccess(String publicUrl) {
                saveNewsFields(existing, fields(title, summary, body, status, publicUrl), callback);
            }

            @Override
            public void onError(@NonNull String error) {
                callback.onError(error);
            }
        });
    }

    public void archiveNews(NewsPost post, ActionCallback callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("status", NewsPost.STATUS_ARCHIVED);
        saveNewsFields(post, fields, callback);
    }

    public void deleteNews(NewsPost post, ActionCallback callback) {
        api.deleteNews("eq." + post.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Не удалось удалить новость: HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    private void saveNewsFields(@Nullable NewsPost existing, Map<String, Object> fields,
                                ActionCallback callback) {
        Call<List<NewsPost>> call = existing == null
                ? api.createNews("*", fields)
                : api.updateNews("eq." + existing.getId(), "*", fields);
        call.enqueue(new Callback<List<NewsPost>>() {
            @Override
            public void onResponse(Call<List<NewsPost>> call, Response<List<NewsPost>> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Не удалось сохранить новость: HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<List<NewsPost>> call, Throwable throwable) {
                callback.onError("Ошибка сети: " + throwable.getMessage());
            }
        });
    }

    private Map<String, Object> fields(String title, String summary, String body,
                                       String status, @Nullable String imageUrl) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("title", title);
        fields.put("summary", summary);
        fields.put("body", body);
        fields.put("status", status);
        if (imageUrl != null) {
            fields.put("image_url", imageUrl);
        }
        return fields;
    }

    private <T> void enqueueList(Call<List<T>> call, NewsListCallback callback, String label) {
        call.enqueue(new Callback<List<T>>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess((List<NewsPost>) response.body());
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
}
