package com.crossfitarmyjym.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.crossfitarmyjym.app.data.repository.AuthRepository;
import com.crossfitarmyjym.app.ui.admin.AdminMainActivity;
import com.crossfitarmyjym.app.ui.auth.LoginActivity;
import com.crossfitarmyjym.app.ui.client.ClientMainActivity;
import com.crossfitarmyjym.app.ui.trainer.TrainerMainActivity;

/**
 * Начальная активность (Splash Screen).
 * Проверяет авторизацию и перенаправляет на соответствующий экран.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 1500; // 1.5 секунды

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "SplashActivity created");
        
        // Запускаем проверку авторизации с задержкой
        handler.postDelayed(this::checkAuthorization, SPLASH_DELAY);
    }

    /**
     * Проверка авторизации пользователя.
     */
    private void checkAuthorization() {
        Log.d(TAG, "Checking authorization status");
        
        AuthRepository authRepository = AuthRepository.getInstance();
        PreferencesManager prefsManager = PreferencesManager.getInstance();
        
        if (prefsManager.isLoggedIn() && prefsManager.getAuthToken() != null) {
            // Пользователь вошел, проверяем валидность токена
            authRepository.getCurrentUser(new AuthRepository.UserCallback() {
                @Override
                public void onSuccess(@NonNull User user) {
                    Log.d(TAG, "User authenticated: " + user.getEmail() + " (role: " + user.getRole() + ")");
                    navigateToMainActivity(user.getRole());
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    Log.w(TAG, "Token validation failed: " + errorMessage);
                    navigateToLogin();
                }
            });
        } else {
            // Пользователь не вошел
            Log.d(TAG, "User not logged in");
            navigateToLogin();
        }
    }

    /**
     * Навигация на главный экран в зависимости от роли.
     * @param role роль пользователя (athlete, trainer, admin)
     */
    private void navigateToMainActivity(@NonNull String role) {
        Intent intent;
        
        switch (role) {
            case "athlete":
                intent = new Intent(this, ClientMainActivity.class);
                break;
            case "trainer":
                intent = new Intent(this, TrainerMainActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminMainActivity.class);
                break;
            default:
                Log.e(TAG, "Unknown role: " + role);
                navigateToLogin();
                return;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Навигация на экран входа.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
