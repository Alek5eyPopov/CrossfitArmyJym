package com.crossfitarmyjym.app.ui.trainer;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.databinding.ActivityTrainerMainBinding;

/**
 * Главная активность для тренера.
 * Содержит навигацию между экранами: Мои занятия, WOD редактор, Клиенты, Профиль.
 */
public class TrainerMainActivity extends AppCompatActivity {

    private static final String TAG = "TrainerMainActivity";

    private ActivityTrainerMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityTrainerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Log.d(TAG, "TrainerMainActivity created");
    }
}