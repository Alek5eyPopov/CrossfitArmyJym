package com.crossfitarmyjym.app.ui.client;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.crossfitarmyjym.app.data.repository.AuthRepository;
import com.crossfitarmyjym.app.databinding.FragmentProfileBinding;
import com.crossfitarmyjym.app.ui.auth.LoginActivity;
import com.crossfitarmyjym.app.ui.progress.ProgressFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private BookingsViewModel bookingsViewModel;
    private ProfileViewModel profileViewModel;
    private PreferencesManager preferencesManager;
    private final List<Exercise> exercises = new ArrayList<>();

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesManager = PreferencesManager.getInstance();
        bookingsViewModel = new ViewModelProvider(this).get(BookingsViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        loadUserProfile();
        setupObservers();
        setupClickListeners();

        bookingsViewModel.loadMyBookings();
        profileViewModel.loadProgress();
    }

    private void loadUserProfile() {
        String name = preferencesManager.getUserName();
        String email = preferencesManager.getUserEmail();
        String role = preferencesManager.getUserRole();

        if (name != null) {
            binding.tvName.setText(name);
        } else {
            binding.tvName.setText(email != null ? email : "Пользователь");
        }

        if (email != null) {
            binding.tvEmail.setText(email);
        }
        binding.tvRole.setText("athlete".equals(role)
                ? getString(R.string.athlete_role)
                : roleLabel(role).toUpperCase(Locale.getDefault()));
        binding.tvAccountRole.setText(roleLabel(role));
    }

    private void setupObservers() {
        bookingsViewModel.getBookings().observe(getViewLifecycleOwner(), this::updateBookingsStats);

        bookingsViewModel.getCancelStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
                if (status.equals("Запись отменена")) {
                    bookingsViewModel.loadMyBookings();
                }
            }
        });

        bookingsViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getExercises().observe(getViewLifecycleOwner(), value -> {
            exercises.clear();
            if (value != null) {
                exercises.addAll(value);
            }
        });

        profileViewModel.getPersonalRecordBests().observe(getViewLifecycleOwner(), records -> {
            int count = records != null ? records.size() : 0;
            binding.tvPrCount.setText(String.valueOf(count));
            binding.tvPrBests.setText(ProgressFormatter.personalRecords(requireContext(), records));
        });

        profileViewModel.getPersonalRecordHistory().observe(getViewLifecycleOwner(), records -> {
            binding.tvPrHistory.setText(ProgressFormatter.personalRecords(requireContext(), records));
        });

        profileViewModel.getWodResults().observe(getViewLifecycleOwner(), results -> {
            binding.tvWodResults.setText(ProgressFormatter.wodResults(requireContext(), results));
        });

        profileViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.btnAddPr.setEnabled(!Boolean.TRUE.equals(loading));
        });

        profileViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnAddPr.setOnClickListener(v -> showAddPrDialog());
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void updateBookingsStats(List<Booking> bookings) {
        if (bookings != null) {
            long activeCount = 0;
            for (Booking booking : bookings) {
                if (booking.isConfirmed()) {
                    activeCount++;
                }
            }
            binding.tvWorkoutsCount.setText(String.valueOf(activeCount));
        }
    }

    private String roleLabel(String role) {
        if ("trainer".equals(role)) {
            return getString(R.string.role_trainer);
        }
        if ("admin".equals(role)) {
            return getString(R.string.role_admin);
        }
        return getString(R.string.role_client);
    }

    private void showAddPrDialog() {
        if (exercises.isEmpty()) {
            Toast.makeText(requireContext(), "Сначала добавьте упражнения в базе", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, 0);

        Spinner exerciseSpinner = new Spinner(requireContext());
        ArrayAdapter<Exercise> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                exercises
        );
        exerciseSpinner.setAdapter(adapter);
        content.addView(exerciseSpinner);

        EditText resultTextInput = createDialogInput("Результат, например 100 кг");
        resultTextInput.setInputType(InputType.TYPE_CLASS_TEXT);
        content.addView(resultTextInput);

        EditText resultValueInput = createDialogInput("Числовое значение, необязательно");
        resultValueInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        content.addView(resultValueInput);

        EditText unitInput = createDialogInput("Единица, например кг / сек / повт.");
        content.addView(unitInput);

        EditText achievedAtInput = createDialogInput("Дата, YYYY-MM-DD");
        achievedAtInput.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        achievedAtInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        content.addView(achievedAtInput);

        EditText notesInput = createDialogInput("Комментарий, необязательно");
        notesInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesInput.setMinLines(2);
        content.addView(notesInput);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_pr))
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Exercise selected = (Exercise) exerciseSpinner.getSelectedItem();
                    String resultText = resultTextInput.getText().toString().trim();
                    if (selected == null || resultText.isEmpty()) {
                        Toast.makeText(requireContext(), "Укажите упражнение и результат", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    profileViewModel.submitPersonalRecord(
                            selected.getId(),
                            parseDoubleOrNull(resultValueInput.getText().toString()),
                            resultText,
                            unitInput.getText().toString().trim(),
                            achievedAtInput.getText().toString().trim(),
                            notesInput.getText().toString().trim()
                    );
                })
                .show();
    }

    private EditText createDialogInput(String hint) {
        EditText input = new EditText(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (10 * getResources().getDisplayMetrics().density);
        input.setLayoutParams(params);
        input.setHint(hint);
        input.setSingleLine(false);
        return input;
    }

    @Nullable
    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void logout() {
        binding.btnLogout.setEnabled(false);
        AuthRepository.getInstance().logout(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@Nullable User user) {
                navigateToLogin();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        if (!isAdded()) {
            return;
        }
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
