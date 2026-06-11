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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.databinding.FragmentAttendanceBinding;

public class AttendanceFragment extends Fragment {

    private FragmentAttendanceBinding binding;
    private AttendanceViewModel viewModel;
    private AttendanceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        adapter = new AttendanceAdapter(viewModel::setAttended);
        binding.rvAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendance.setAdapter(adapter);
        binding.btnSaveAttendance.setOnClickListener(v -> viewModel.saveAttendance());

        Bundle args = getArguments();
        String classId = args != null ? args.getString("class_id") : null;
        String classTitle = args != null ? args.getString("class_title") : null;
        if (classTitle != null && !classTitle.isEmpty()) {
            binding.tvClassTitle.setText(classTitle);
        }

        viewModel.getAttendanceList().observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);
            boolean empty = entries == null || entries.isEmpty();
            binding.rvAttendance.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.btnSaveAttendance.setEnabled(!empty);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSaveAttendance.setEnabled(!loading
                    && adapter.getItemCount() > 0);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showMessage);
        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), this::showMessage);

        if (classId == null || classId.isEmpty()) {
            showMessage("Занятие не выбрано");
        } else {
            viewModel.loadAttendance(classId);
        }
    }

    private void showMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
