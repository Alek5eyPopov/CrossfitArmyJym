package com.crossfitarmyjym.app.ui.client;

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

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.FragmentScheduleBinding;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Фрагмент расписания занятий.
 * Отображает список доступных занятий с возможностью записи.
 */
public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private ScheduleViewModel viewModel;
    private ScheduleAdapter adapter;

    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        setupRecyclerView();
        setupObservers();
        binding.btnRefresh.setOnClickListener(v -> viewModel.refreshSchedule());

        viewModel.loadSchedule();
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(viewModel::bookClass);

        binding.rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSchedule.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getClasses().observe(getViewLifecycleOwner(), gymClasses -> {
            if (gymClasses != null) {
                updateAdapter(gymClasses, viewModel.getBookedClassIds().getValue());
                boolean empty = gymClasses.isEmpty();
                binding.rvSchedule.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getBookedClassIds().observe(getViewLifecycleOwner(), bookedIds ->
                updateAdapter(viewModel.getClasses().getValue(), bookedIds));

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRefresh.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getBookingStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapter(List<GymClass> classes, Set<String> bookedIds) {
        adapter.submitData(
                classes != null ? classes : Collections.emptyList(),
                bookedIds != null ? bookedIds : Collections.emptySet());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
