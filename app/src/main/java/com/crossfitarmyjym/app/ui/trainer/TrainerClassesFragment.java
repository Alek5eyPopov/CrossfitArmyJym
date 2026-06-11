package com.crossfitarmyjym.app.ui.trainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.FragmentMyClassesBinding;
import com.crossfitarmyjym.app.ui.client.ScheduleAdapter;

public class TrainerClassesFragment extends Fragment {

    private FragmentMyClassesBinding binding;
    private TrainerClassesViewModel viewModel;
    private TrainerClassesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyClassesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TrainerClassesViewModel.class);
        adapter = new TrainerClassesAdapter(this::openAttendance);
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);

        viewModel.getClasses().observe(getViewLifecycleOwner(), classes -> {
            adapter.submitList(classes);
            boolean empty = classes == null || classes.isEmpty();
            binding.rvClasses.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadMyClasses();
    }

    private void openAttendance(GymClass gymClass) {
        Bundle args = new Bundle();
        args.putString("class_id", gymClass.getId());
        args.putString("class_title", ScheduleAdapter.formatTime(gymClass.getScheduledStart()));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_classes_to_attendance, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
