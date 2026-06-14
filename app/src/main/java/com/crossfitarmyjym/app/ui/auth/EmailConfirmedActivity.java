package com.crossfitarmyjym.app.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.databinding.ActivityEmailConfirmedBinding;

public class EmailConfirmedActivity extends AppCompatActivity {

    private ActivityEmailConfirmedBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailConfirmedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        renderResult(getIntent());
        binding.btnLogin.setOnClickListener(view -> openLogin());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        renderResult(intent);
    }

    private void renderResult(@Nullable Intent intent) {
        String error = readParameter(intent != null ? intent.getData() : null, "error_description");
        if (error == null) {
            error = readParameter(intent != null ? intent.getData() : null, "error");
        }

        boolean confirmed = error == null || error.trim().isEmpty();
        binding.ivStatus.setImageResource(
                confirmed ? R.drawable.army_emblem_white : android.R.drawable.ic_dialog_alert
        );
        binding.tvTitle.setText(confirmed
                ? R.string.email_confirmed_title
                : R.string.email_confirmation_failed_title);
        binding.tvMessage.setText(confirmed
                ? R.string.email_confirmed_message
                : R.string.email_confirmation_failed_message);
        binding.btnLogin.setText(confirmed
                ? R.string.email_confirmed_login
                : R.string.email_confirmation_back);
    }

    @Nullable
    private String readParameter(@Nullable Uri uri, String name) {
        if (uri == null) {
            return null;
        }

        String queryValue = uri.getQueryParameter(name);
        if (queryValue != null) {
            return queryValue;
        }

        String fragment = uri.getFragment();
        if (fragment == null || fragment.isEmpty()) {
            return null;
        }

        for (String pair : fragment.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) {
                return Uri.decode(parts[1]);
            }
        }
        return null;
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
