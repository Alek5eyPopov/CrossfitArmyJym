package com.crossfitarmyjym.app.ui.trainer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.WodExerciseInput;
import com.crossfitarmyjym.app.databinding.FragmentWodEditorBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WodEditorFragment extends Fragment {

    private static final String[] FORMATS = {"amrap", "emom", "for_time", "tabata", "ladder"};

    private FragmentWodEditorBinding binding;
    private WodEditorViewModel viewModel;
    private final List<Group> groups = new ArrayList<>();
    private final List<Exercise> exercises = new ArrayList<>();
    private final List<WodExerciseInput> selectedExercises = new ArrayList<>();
    private final List<String> selectedLabels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWodEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WodEditorViewModel.class);
        binding.etScheduledDate.setText(
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date())
        );
        binding.spinnerFormat.setAdapter(spinnerAdapter(Arrays.asList(FORMATS)));
        binding.btnAddExercise.setOnClickListener(v -> addExercise());
        binding.btnSaveWod.setOnClickListener(v -> saveWod());
        observe();
        viewModel.loadReferenceData();
    }

    private void observe() {
        viewModel.getGroups().observe(getViewLifecycleOwner(), value -> {
            groups.clear();
            if (value != null) groups.addAll(value);
            binding.spinnerGroup.setAdapter(spinnerAdapter(groups));
        });
        viewModel.getExercises().observe(getViewLifecycleOwner(), value -> {
            exercises.clear();
            if (value != null) exercises.addAll(value);
            binding.spinnerExercise.setAdapter(spinnerAdapter(exercises));
        });
        viewModel.getIsSaving().observe(getViewLifecycleOwner(), saving -> {
            binding.btnSaveWod.setEnabled(!Boolean.TRUE.equals(saving));
            binding.progressBar.setVisibility(Boolean.TRUE.equals(saving) ? View.VISIBLE : View.GONE);
        });
        viewModel.getSaveResult().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                selectedExercises.clear();
                selectedLabels.clear();
                updateExerciseSummary();
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private <T> ArrayAdapter<T> spinnerAdapter(List<T> values) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void addExercise() {
        int position = binding.spinnerExercise.getSelectedItemPosition();
        if (position < 0 || position >= exercises.size()) {
            Toast.makeText(requireContext(), "Список упражнений пуст", Toast.LENGTH_SHORT).show();
            return;
        }
        Exercise exercise = exercises.get(position);
        for (WodExerciseInput item : selectedExercises) {
            if (exercise.getId().equals(item.getExerciseId())) {
                Toast.makeText(requireContext(), "Упражнение уже добавлено", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int rounds = parseInt(binding.etRounds.getText(), 1);
        int weight = parseInt(binding.etWeight.getText(), 0);
        selectedExercises.add(new WodExerciseInput(exercise.getId(), rounds, weight, null));
        selectedLabels.add(exercise.getName() + ": " + rounds + " раунд., " + weight + " кг");
        updateExerciseSummary();
    }

    private void updateExerciseSummary() {
        binding.tvSelectedExercises.setText(selectedLabels.isEmpty()
                ? "Упражнения не добавлены"
                : TextUtils.join("\n", selectedLabels));
    }

    private void saveWod() {
        String name = Objects.requireNonNull(binding.etWodName.getText()).toString().trim();
        if (name.isEmpty()) {
            binding.tilWodName.setError("Введите название WOD");
            return;
        }
        binding.tilWodName.setError(null);
        int groupPosition = binding.spinnerGroup.getSelectedItemPosition();
        if (groupPosition < 0 || groupPosition >= groups.size()) {
            Toast.makeText(requireContext(), "Выберите доступную группу", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = Objects.requireNonNull(binding.etScheduledDate.getText()).toString().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(requireContext(), "Дата должна быть в формате YYYY-MM-DD",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int minutes = parseInt(binding.etTimeCap.getText(), 0);
        String notes = Objects.requireNonNull(binding.etWodDescription.getText()).toString().trim();
        viewModel.createWod(
                name,
                String.valueOf(binding.spinnerFormat.getSelectedItem()),
                groups.get(groupPosition).getId(),
                date,
                minutes * 60,
                notes,
                new ArrayList<>(selectedExercises)
        );
    }

    private int parseInt(CharSequence value, int fallback) {
        try {
            return Math.max(Integer.parseInt(value == null ? "" : value.toString()), 0);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
