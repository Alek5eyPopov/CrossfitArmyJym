package com.crossfitarmyjym.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.databinding.FragmentStatisticsBinding;

/**
 * Фрагмент статистики (админ-панель).
 * Отображает общую статистику по залу.
 */
public class AdminStatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private AdminStatisticsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminStatisticsViewModel.class);

        setupObservers();

        viewModel.loadStatistics();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: показать/скрыть ProgressBar
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getTotalUsers().observe(getViewLifecycleOwner(), count -> {
            binding.tvUsersCount.setText(String.valueOf(count));
        });

        viewModel.getTotalClasses().observe(getViewLifecycleOwner(), count -> {
            // TODO: добавить TextView для классов
        });

        viewModel.getTotalBookings().observe(getViewLifecycleOwner(), count -> {
            binding.tvAttendanceCount.setText(String.valueOf(count));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}