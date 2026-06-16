package com.crossfitarmyjym.app.ui.trainer;

import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.WodTaskInput;
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
    private static final String NO_OPTIONAL_EXERCISE = "Без optional";

    private FragmentWodEditorBinding binding;
    private WodEditorViewModel viewModel;
    private final List<Group> groups = new ArrayList<>();
    private final List<Exercise> exercises = new ArrayList<>();
    private final List<Exercise> optionalExercises = new ArrayList<>();
    private final List<LoadType> loadTypes = new ArrayList<>();
    private final List<WodTaskInput> selectedTasks = new ArrayList<>();
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
        binding.btnAddTask.setOnClickListener(v -> addTask());
        binding.btnCreateExercise.setOnClickListener(v -> showCreateExerciseDialog());
        binding.btnCreateLoadType.setOnClickListener(v -> showCreateLoadTypeDialog());
        binding.btnCreateTaskTemplate.setOnClickListener(v -> saveCurrentTaskAsTemplate());
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
            optionalExercises.clear();
            if (value != null) {
                exercises.addAll(value);
                optionalExercises.addAll(value);
            }
            binding.spinnerRxExercise.setAdapter(spinnerAdapter(exercises));
            binding.spinnerOptionalExercise.setAdapter(optionalExerciseAdapter());
        });
        viewModel.getLoadTypes().observe(getViewLifecycleOwner(), value -> {
            loadTypes.clear();
            if (value != null) loadTypes.addAll(value);
            binding.spinnerLoadType.setAdapter(spinnerAdapter(loadTypes));
        });
        viewModel.getIsSaving().observe(getViewLifecycleOwner(), saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            binding.btnSaveWod.setEnabled(!isSaving);
            binding.btnAddTask.setEnabled(!isSaving);
            binding.btnCreateExercise.setEnabled(!isSaving);
            binding.btnCreateLoadType.setEnabled(!isSaving);
            binding.btnCreateTaskTemplate.setEnabled(!isSaving);
            binding.progressBar.setVisibility(isSaving ? View.VISIBLE : View.GONE);
        });
        viewModel.getSaveResult().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                selectedTasks.clear();
                selectedLabels.clear();
                updateTaskSummary();
                clearTaskInputs();
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

    private ArrayAdapter<String> optionalExerciseAdapter() {
        List<String> labels = new ArrayList<>();
        labels.add(NO_OPTIONAL_EXERCISE);
        for (Exercise exercise : optionalExercises) {
            labels.add(exercise.getName());
        }
        return spinnerAdapter(labels);
    }

    private void addTask() {
        int exercisePosition = binding.spinnerRxExercise.getSelectedItemPosition();
        int loadTypePosition = binding.spinnerLoadType.getSelectedItemPosition();
        if (exercisePosition < 0 || exercisePosition >= exercises.size()) {
            Toast.makeText(requireContext(), "Список упражнений пуст", Toast.LENGTH_SHORT).show();
            return;
        }
        if (loadTypePosition < 0 || loadTypePosition >= loadTypes.size()) {
            Toast.makeText(requireContext(), "Список типов нагрузки пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        Exercise rxExercise = exercises.get(exercisePosition);
        LoadType loadType = loadTypes.get(loadTypePosition);
        String title = text(binding.etTaskTitle.getText());
        String rxLoad = text(binding.etRxLoad.getText());
        if (rxLoad.isEmpty()) {
            binding.tilRxLoad.setError("Опишите RX нагрузку");
            return;
        }
        binding.tilRxLoad.setError(null);

        Exercise optionalExercise = selectedOptionalExercise();
        String optionalLoad = text(binding.etOptionalLoad.getText());
        String notes = text(binding.etTaskNotes.getText());
        int position = selectedTasks.size() + 1;
        String taskTitle = title.isEmpty() ? rxExercise.getName() : title;

        selectedTasks.add(WodTaskInput.direct(
                position,
                taskTitle,
                rxExercise.getId(),
                loadType.getId(),
                rxLoad,
                optionalExercise == null ? null : optionalExercise.getId(),
                optionalExercise == null ? null : loadType.getId(),
                optionalLoad,
                notes
        ));
        selectedLabels.add(buildTaskLabel(position, taskTitle, rxExercise, loadType,
                rxLoad, optionalExercise, optionalLoad));
        updateTaskSummary();
        clearTaskInputs();
    }

    private Exercise selectedOptionalExercise() {
        int position = binding.spinnerOptionalExercise.getSelectedItemPosition();
        int exerciseIndex = position - 1;
        if (exerciseIndex < 0 || exerciseIndex >= optionalExercises.size()) {
            return null;
        }
        return optionalExercises.get(exerciseIndex);
    }

    private String buildTaskLabel(int position, String title, Exercise rxExercise,
                                  LoadType loadType, String rxLoad,
                                  Exercise optionalExercise, String optionalLoad) {
        StringBuilder builder = new StringBuilder();
        builder.append(position).append(". ").append(title).append('\n')
                .append("RX: ").append(rxExercise.getName())
                .append(" • ").append(loadType.getName())
                .append(" • ").append(rxLoad);
        if (optionalExercise != null || !optionalLoad.isEmpty()) {
            builder.append('\n').append("Optional: ");
            builder.append(optionalExercise == null ? rxExercise.getName() : optionalExercise.getName());
            if (!optionalLoad.isEmpty()) {
                builder.append(" • ").append(optionalLoad);
            }
        }
        return builder.toString();
    }

    private void updateTaskSummary() {
        binding.tvSelectedTasks.setText(selectedLabels.isEmpty()
                ? getString(com.crossfitarmyjym.app.R.string.wod_no_tasks)
                : TextUtils.join("\n\n", selectedLabels));
    }

    private void clearTaskInputs() {
        binding.etTaskTitle.setText("");
        binding.etRxLoad.setText("");
        binding.etOptionalLoad.setText("");
        binding.etTaskNotes.setText("");
        binding.spinnerOptionalExercise.setSelection(0);
    }

    private void showCreateExerciseDialog() {
        LinearLayout content = dialogContent();
        EditText name = field("Название упражнения", null);
        Spinner category = spinnerAdapterView(Arrays.asList(
                "gymnastics", "weightlifting", "cardio", "monostructural"
        ));
        EditText description = field("Описание", null);
        EditText unitType = field("Единица результата: kg, reps, time", "reps");
        EditText prUnit = field("Единица PR: kg, reps, sec", null);
        Spinner direction = spinnerAdapterView(Arrays.asList("max", "min"));
        content.addView(name);
        content.addView(category);
        content.addView(description);
        content.addView(unitType);
        content.addView(prUnit);
        content.addView(direction);

        new AlertDialog.Builder(requireContext())
                .setTitle("Новое упражнение")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {
                    String exerciseName = text(name.getText());
                    if (exerciseName.isEmpty()) {
                        toast("Введите название упражнения");
                        return;
                    }
                    viewModel.createExercise(
                            exerciseName,
                            String.valueOf(category.getSelectedItem()),
                            text(description.getText()),
                            text(unitType.getText()).isEmpty() ? "reps" : text(unitType.getText()),
                            text(prUnit.getText()),
                            String.valueOf(direction.getSelectedItem())
                    );
                })
                .show();
    }

    private void showCreateLoadTypeDialog() {
        LinearLayout content = dialogContent();
        EditText code = field("Код: emom_every_3_min", null);
        EditText name = field("Название: EMOM каждые 3 минуты", null);
        EditText description = field("Описание", null);
        content.addView(code);
        content.addView(name);
        content.addView(description);

        new AlertDialog.Builder(requireContext())
                .setTitle("Новый тип нагрузки")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {
                    String loadCode = normalizeCode(text(code.getText()));
                    String loadName = text(name.getText());
                    if (loadCode.isEmpty() || loadName.isEmpty()) {
                        toast("Введите код и название типа нагрузки");
                        return;
                    }
                    viewModel.createLoadType(loadCode, loadName, text(description.getText()));
                })
                .show();
    }

    private void saveCurrentTaskAsTemplate() {
        int exercisePosition = binding.spinnerRxExercise.getSelectedItemPosition();
        int loadTypePosition = binding.spinnerLoadType.getSelectedItemPosition();
        if (exercisePosition < 0 || exercisePosition >= exercises.size()
                || loadTypePosition < 0 || loadTypePosition >= loadTypes.size()) {
            toast("Выберите упражнение и тип нагрузки");
            return;
        }
        Exercise rxExercise = exercises.get(exercisePosition);
        LoadType loadType = loadTypes.get(loadTypePosition);
        String title = text(binding.etTaskTitle.getText());
        String rxLoad = text(binding.etRxLoad.getText());
        if (rxLoad.isEmpty()) {
            binding.tilRxLoad.setError("Опишите RX нагрузку");
            return;
        }
        binding.tilRxLoad.setError(null);
        Exercise optionalExercise = selectedOptionalExercise();
        String optionalLoad = text(binding.etOptionalLoad.getText());
        String notes = text(binding.etTaskNotes.getText());
        viewModel.createTrainingTask(
                title.isEmpty() ? rxExercise.getName() : title,
                rxExercise.getId(),
                loadType.getId(),
                rxLoad,
                optionalExercise == null ? null : optionalExercise.getId(),
                optionalExercise == null ? null : loadType.getId(),
                optionalLoad,
                notes
        );
    }

    private LinearLayout dialogContent() {
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, 0, padding, 0);
        return content;
    }

    private EditText field(String hint, String value) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        if (value != null) editText.setText(value);
        return editText;
    }

    private Spinner spinnerAdapterView(List<String> values) {
        Spinner spinner = new Spinner(requireContext());
        spinner.setAdapter(spinnerAdapter(values));
        return spinner;
    }

    private String normalizeCode(String value) {
        return value.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private void toast(String value) {
        Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
    }

    private void saveWod() {
        String name = text(binding.etWodName.getText());
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
        String date = text(binding.etScheduledDate.getText());
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
                new ArrayList<>(selectedTasks)
        );
    }

    private String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
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
