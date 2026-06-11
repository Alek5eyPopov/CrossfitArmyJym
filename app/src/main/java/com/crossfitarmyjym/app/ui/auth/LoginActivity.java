package com.crossfitarmyjym.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.repository.AuthRepository;
import com.crossfitarmyjym.app.databinding.ActivityLoginBinding;
import com.crossfitarmyjym.app.ui.admin.AdminMainActivity;
import com.crossfitarmyjym.app.ui.client.ClientMainActivity;
import com.crossfitarmyjym.app.ui.trainer.TrainerMainActivity;

import java.util.Objects;

/**
 * Активность входа в систему.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        authRepository = AuthRepository.getInstance();
        
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Кнопка входа
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        
        // Ссылка на регистрацию
        binding.tvSignupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    /**
     * Попытка входа пользователя.
     */
    private void attemptLogin() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

        // Валидация
        if (validateInput(email, password)) {
            showLoading(true);
            performLogin(email, password);
        }
    }

    /**
     * Валидация введенных данных.
     */
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            return false;
        }
        
        if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            return false;
        }
        
        // Сбрасываем ошибки
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        
        return true;
    }

    /**
     * Выполнение входа через репозиторий.
     */
    private void performLogin(String email, String password) {
        Log.d(TAG, "Performing login for email: " + email);
        
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@Nullable User user) {
                Log.d(TAG, "Login successful");
                showLoading(false);
                if (user == null) {
                    showToast("Не удалось загрузить профиль");
                    return;
                }
                navigateToMainActivity(user);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Login failed: " + errorMessage);
                showLoading(false);
                showToast(errorMessage);
            }
        });
    }

    /**
     * Навигация на главный экран в зависимости от роли.
     */
    private void navigateToMainActivity(User user) {
        Intent intent;
        
        switch (user.getRole()) {
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
                showToast("Неизвестная роль");
                return;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Отображение состояния загрузки.
     */
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }

    /**
     * Отображение Toast сообщения.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
