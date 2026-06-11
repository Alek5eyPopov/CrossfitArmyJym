package com.crossfitarmyjym.app.ui.admin;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
        Log.d(TAG, "AdminMainActivity created");
    }
}
