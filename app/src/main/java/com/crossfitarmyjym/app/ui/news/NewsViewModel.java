package com.crossfitarmyjym.app.ui.news;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.crossfitarmyjym.app.data.model.NewsPost;
import com.crossfitarmyjym.app.data.repository.NewsRepository;

import java.util.Collections;
import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private final NewsRepository repository;
    private final MutableLiveData<List<NewsPost>> posts =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<NewsPost> selectedPost = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public NewsViewModel(@NonNull Application application) {
        super(application);
        repository = NewsRepository.getInstance(application);
    }

    public LiveData<List<NewsPost>> getPosts() {
        return posts;
    }

    public LiveData<NewsPost> getSelectedPost() {
        return selectedPost;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadNews() {
        loading.setValue(true);
        repository.getPublishedNews(new NewsRepository.NewsListCallback() {
            @Override
            public void onSuccess(List<NewsPost> value) {
                posts.postValue(value);
                loading.postValue(false);
            }

            @Override
            public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
    }

    public void loadNewsPost(String id) {
        loading.setValue(true);
        repository.getNewsById(id, new NewsRepository.NewsPostCallback() {
            @Override
            public void onSuccess(NewsPost post) {
                selectedPost.postValue(post);
                loading.postValue(false);
            }

            @Override
            public void onError(@NonNull String value) {
                error.postValue(value);
                loading.postValue(false);
            }
        });
    }
}
