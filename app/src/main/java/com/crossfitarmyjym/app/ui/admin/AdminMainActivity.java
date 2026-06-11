package com.crossfitarmyjym.app.ui.admin;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.databinding.ActivityAdminMainBinding;

/**
 * Главная активность для администратора.
 * Содержит навигацию между экранами: Пользователи, Расписание, Статистика, Контент.
 */
public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";

    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Log.d(TAG, "AdminMainActivity created");
    }
}