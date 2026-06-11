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
import com.crossfitarmyjym.app.databinding.ActivitySignupBinding;
import com.crossfitarmyjym.app.ui.admin.AdminMainActivity;
import com.crossfitarmyjym.app.ui.client.ClientMainActivity;
import com.crossfitarmyjym.app.ui.trainer.TrainerMainActivity;

import java.util.Objects;

/**
 * Активность регистрации нового пользователя.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private ActivitySignupBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        authRepository = AuthRepository.getInstance();
        
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Кнопка регистрации
        binding.btnSignup.setOnClickListener(v -> attemptSignup());
        
        // Ссылка на вход
        binding.tvLoginLink.setOnClickListener(v -> {
            finish(); // Возвращаемся на экран входа
        });
    }

    /**
     * Попытка регистрации пользователя.
     */
    private void attemptSignup() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();
        String fullName = Objects.requireNonNull(binding.etFullName.getText()).toString().trim();

        // Валидация
        if (validateInput(email, password, confirmPassword, fullName)) {
            showLoading(true);
            performSignup(email, password, fullName);
        }
    }

    /**
     * Валидация введенных данных.
     */
    private boolean validateInput(String email, String password, String confirmPassword, String fullName) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_mismatch));
            isValid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        // Полное имя опционально, но если введено - проверяем
        if (!TextUtils.isEmpty(fullName) && fullName.length() < 2) {
            binding.tilFullName.setError(getString(R.string.error_name_too_short));
            isValid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        return isValid;
    }

    /**
     * Выполнение регистрации через репозиторий.
     */
    private void performSignup(String email, String password, String fullName) {
        Log.d(TAG, "Performing signup for email: " + email);
        
        // Если имя не введено, передаем null
        String nameToUse = TextUtils.isEmpty(fullName) ? null : fullName;
        
        authRepository.signup(email, password, nameToUse, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@Nullable User user) {
                Log.d(TAG, "Signup successful");
                showLoading(false);
                if (user == null) {
                    showToast("Не удалось загрузить профиль");
                    return;
                }
                navigateToMainActivity(user);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Signup failed: " + errorMessage);
                showLoading(false);
                showToast(errorMessage);
            }

            @Override
            public void onEmailConfirmationRequired() {
                showLoading(false);
                showToast("Подтвердите email, затем войдите в приложение");
                finish();
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
        binding.btnSignup.setEnabled(!isLoading);
        setFieldsEnabled(!isLoading);
    }

    /**
     * Включение/выключение полей ввода.
     */
    private void setFieldsEnabled(boolean enabled) {
        binding.etEmail.setEnabled(enabled);
        binding.etPassword.setEnabled(enabled);
        binding.etConfirmPassword.setEnabled(enabled);
        binding.etFullName.setEnabled(enabled);
    }

    /**
     * Отображение Toast сообщения.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
