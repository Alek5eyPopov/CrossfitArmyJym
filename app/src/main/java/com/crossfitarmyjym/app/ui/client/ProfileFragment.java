package com.crossfitarmyjym.app.ui.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.crossfitarmyjym.app.data.repository.AuthRepository;
import com.crossfitarmyjym.app.data.repository.ResultRepository;
import com.crossfitarmyjym.app.databinding.FragmentProfileBinding;
import com.crossfitarmyjym.app.ui.auth.LoginActivity;

import java.util.List;

/**
 * Фрагмент профиля пользователя.
 * Отображает информацию о пользователе, статистику, записи и кнопку выхода.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private BookingsViewModel bookingsViewModel;
    private PreferencesManager preferencesManager;
    private ResultRepository resultRepository;

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
        resultRepository = new ResultRepository();

        loadUserProfile();
        setupObservers();
        setupClickListeners();

        bookingsViewModel.loadMyBookings();
        loadResultStats();
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

        // Статистика (заглушка, будет заполняться реальными данными позже)
        // Можно загрузить через ResultRepository
    }

    private void setupObservers() {
        bookingsViewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            updateBookingsStats(bookings);
        });

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
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void updateBookingsStats(List<Booking> bookings) {
        if (bookings != null) {
            long activeCount = bookings.stream().filter(Booking::isConfirmed).count();
            binding.tvWorkoutsCount.setText(String.valueOf(activeCount));

            // Считаем PR из bookings (заглушка)
        }
    }

    private void loadResultStats() {
        resultRepository.getMyResults(new ResultRepository.ResultsCallback() {
            @Override
            public void onSuccess(List<com.crossfitarmyjym.app.data.model.Result> results) {
                long prCount = results.stream()
                        .filter(com.crossfitarmyjym.app.data.model.Result::isPr)
                        .count();
                if (binding != null) {
                    binding.tvPrCount.setText(String.valueOf(prCount));
                }
            }

            @Override
            public void onError(@NonNull String error) {
                if (binding != null) {
                    binding.tvPrCount.setText("0");
                }
            }
        });
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
