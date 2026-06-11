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

public class StatisticsFragment extends Fragment {

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
        viewModel = new ViewModelProvider(this).get(AdminStatisticsViewModel.class);
        binding.btnRefresh.setOnClickListener(v -> viewModel.load());
        viewModel.getLoading().observe(getViewLifecycleOwner(), value ->
                binding.progressBar.setVisibility(Boolean.TRUE.equals(value) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), value -> {
            if (value != null) Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
        });
        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            binding.tvUsersCount.setText(String.valueOf(stats.getUsers()));
            binding.tvActiveUsersCount.setText(String.valueOf(stats.getActiveUsers()));
            binding.tvGroupsCount.setText(String.valueOf(stats.getGroups()));
            binding.tvClassesCount.setText(String.valueOf(stats.getClasses()));
            binding.tvBookingsCount.setText(String.valueOf(stats.getConfirmedBookings()));
            binding.tvAttendanceCount.setText(String.valueOf(stats.getAttended()));
            binding.tvResultsCount.setText(String.valueOf(stats.getResults()));
        });
        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
