package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_client);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        Log.d(TAG, "ClientMainActivity created");
    }
}
