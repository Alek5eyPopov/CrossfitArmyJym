package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.databinding.ActivityClientMainBinding;

/**
 * Главная активность для клиента (атлета).
 * Содержит навигацию между экранами: WOD дня, Расписание, Профиль.
 */
public class ClientMainActivity extends AppCompatActivity {

    private static final String TAG = "ClientMainActivity";

    private ActivityClientMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityClientMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Log.d(TAG, "ClientMainActivity created");
    }
}