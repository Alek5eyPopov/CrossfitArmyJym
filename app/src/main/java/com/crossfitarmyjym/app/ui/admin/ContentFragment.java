package com.crossfitarmyjym.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.Exercise;
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.LoadType;
import com.crossfitarmyjym.app.data.model.TrainingTask;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.repository.AdminRepository;
import com.crossfitarmyjym.app.databinding.FragmentContentBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContentFragment extends Fragment {

    private static final String NO_OPTIONAL = "Без optional";

    private FragmentContentBinding binding;
    private AdminContentViewModel viewModel;
    private List<Group> groups = new ArrayList<>();
    private List<User> trainers = new ArrayList<>();
    private List<Exercise> exercises = new ArrayList<>();
    private List<LoadType> loadTypes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminContentViewModel.class);
        setupLists();
        binding.btnAddGroup.setOnClickListener(v -> showGroupDialog(null));
        binding.btnAddClass.setOnClickListener(v -> showClassDialog(null));
        binding.btnAddExercise.setOnClickListener(v -> showExerciseDialog(null));
        binding.btnAddLoadType.setOnClickListener(v -> showLoadTypeDialog(null));
        binding.btnAddTrainingTask.setOnClickListener(v -> showTrainingTaskDialog(null));
        binding.btnCreateWod.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_content_to_wod_editor));
        observe();
        viewModel.load();
    }

    private void setupLists() {
        setupGroups();
        setupClasses();
        setupExercises();
        setupLoadTypes();
        setupTrainingTasks();
        setupWods();
    }

    private void setupGroups() {
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<Group>() {
                    @Override public String title(Group item) { return item.getName(); }
                    @Override public String subtitle(Group item) {
                        return (item.isActive() ? "Активна" : "Отключена") + " • "
                                + value(item.getSchedule(), "расписание не задано");
                    }
                },
                new AdminRowAdapter.Listener<Group>() {
                    @Override public void onEdit(Group item) { showGroupDialog(item); }
                    @Override public void onDelete(Group item) {
                        confirm("Удалить группу «" + item.getName() + "»?",
                                () -> viewModel.deleteGroup(item));
                    }
                }
        ));
    }

    private void setupClasses() {
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<GymClass>() {
                    @Override public String title(GymClass item) {
                        return value(item.getLocation(), "Занятие");
                    }
                    @Override public String subtitle(GymClass item) {
                        return value(item.getScheduledStart(), "") + " • " + item.getStatus()
                                + " • " + item.getCurrentBookings() + "/" + item.getMaxCapacity();
                    }
                },
                new AdminRowAdapter.Listener<GymClass>() {
                    @Override public void onEdit(GymClass item) { showClassDialog(item); }
                    @Override public void onDelete(GymClass item) {
                        confirm("Удалить занятие?", () -> viewModel.deleteClass(item));
                    }
                }
        ));
    }

    private void setupExercises() {
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExercises.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<Exercise>() {
                    @Override public String title(Exercise item) { return item.getName(); }
                    @Override public String subtitle(Exercise item) {
                        return value(item.getCategory(), "категория") + " • "
                                + value(item.getUnitType(), "unit") + " • "
                                + (item.isActive() ? "активно" : "скрыто");
                    }
                },
                new AdminRowAdapter.Listener<Exercise>() {
                    @Override public void onEdit(Exercise item) { showExerciseDialog(item); }
                    @Override public void onDelete(Exercise item) {
                        confirm("Удалить упражнение «" + item.getName() + "»?",
                                () -> viewModel.deleteExercise(item));
                    }
                }
        ));
    }

    private void setupLoadTypes() {
        binding.rvLoadTypes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLoadTypes.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<LoadType>() {
                    @Override public String title(LoadType item) { return item.getName(); }
                    @Override public String subtitle(LoadType item) {
                        return item.getCode() + " • " + (item.isActive() ? "активно" : "скрыто");
                    }
                },
                new AdminRowAdapter.Listener<LoadType>() {
                    @Override public void onEdit(LoadType item) { showLoadTypeDialog(item); }
                    @Override public void onDelete(LoadType item) {
                        confirm("Удалить тип нагрузки «" + item.getName() + "»?",
                                () -> viewModel.deleteLoadType(item));
                    }
                }
        ));
    }

    private void setupTrainingTasks() {
        binding.rvTrainingTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrainingTasks.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<TrainingTask>() {
                    @Override public String title(TrainingTask item) { return item.getTitle(); }
                    @Override public String subtitle(TrainingTask item) {
                        return nameForExercise(item.getRxExerciseId()) + " • "
                                + nameForLoadType(item.getLoadTypeId()) + " • "
                                + (item.isActive() ? "активно" : "скрыто");
                    }
                },
                new AdminRowAdapter.Listener<TrainingTask>() {
                    @Override public void onEdit(TrainingTask item) { showTrainingTaskDialog(item); }
                    @Override public void onDelete(TrainingTask item) {
                        confirm("Удалить шаблон «" + item.getTitle() + "»?",
                                () -> viewModel.deleteTrainingTask(item));
                    }
                }
        ));
    }

    private void setupWods() {
        binding.rvWods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWods.setAdapter(new AdminRowAdapter<>(
                new AdminRowAdapter.Presenter<Wod>() {
                    @Override public String title(Wod item) { return item.getName(); }
                    @Override public String subtitle(Wod item) {
                        return item.getScheduledDate() + " • " + item.getFormat();
                    }
                },
                new AdminRowAdapter.Listener<Wod>() {
                    @Override public void onEdit(Wod item) { showWodDialog(item); }
                    @Override public void onDelete(Wod item) {
                        confirm("Удалить WOD «" + item.getName() + "»?",
                                () -> viewModel.deleteWod(item));
                    }
                }
        ));
    }

    @SuppressWarnings("unchecked")
    private void observe() {
        viewModel.getGroups().observe(getViewLifecycleOwner(), value -> {
            groups = value != null ? value : new ArrayList<>();
            ((AdminRowAdapter<Group>) binding.rvGroups.getAdapter()).submitList(value);
        });
        viewModel.getClasses().observe(getViewLifecycleOwner(), value ->
                ((AdminRowAdapter<GymClass>) binding.rvClasses.getAdapter()).submitList(value));
        viewModel.getExercises().observe(getViewLifecycleOwner(), value -> {
            exercises = value != null ? value : new ArrayList<>();
            ((AdminRowAdapter<Exercise>) binding.rvExercises.getAdapter()).submitList(value);
        });
        viewModel.getLoadTypes().observe(getViewLifecycleOwner(), value -> {
            loadTypes = value != null ? value : new ArrayList<>();
            ((AdminRowAdapter<LoadType>) binding.rvLoadTypes.getAdapter()).submitList(value);
        });
        viewModel.getTrainingTasks().observe(getViewLifecycleOwner(), value ->
                ((AdminRowAdapter<TrainingTask>) binding.rvTrainingTasks.getAdapter()).submitList(value));
        viewModel.getWods().observe(getViewLifecycleOwner(), value ->
                ((AdminRowAdapter<Wod>) binding.rvWods.getAdapter()).submitList(value));
        viewModel.getTrainers().observe(getViewLifecycleOwner(), value ->
                trainers = value != null ? value : new ArrayList<>());
        viewModel.getLoading().observe(getViewLifecycleOwner(), value ->
                binding.progressBar.setVisibility(Boolean.TRUE.equals(value) ? View.VISIBLE : View.GONE));
        viewModel.getMessage().observe(getViewLifecycleOwner(), this::toast);
        viewModel.getError().observe(getViewLifecycleOwner(), this::toast);
    }

    private void showGroupDialog(Group existing) {
        LinearLayout content = dialogContent();
        EditText name = field("Название", existing == null ? null : existing.getName());
        Spinner trainer = spinner(trainerLabels());
        EditText schedule = field("Расписание", existing == null ? null : existing.getSchedule());
        CheckBox active = checkbox("Группа активна", existing == null || existing.isActive());
        content.addView(name);
        content.addView(trainer);
        content.addView(schedule);
        content.addView(active);
        selectTrainer(trainer, existing == null ? null : existing.getTrainerId());

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Новая группа" : "Изменить группу")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    if (trainers.isEmpty() || text(name).isEmpty()) {
                        toast("Укажите название и добавьте хотя бы одного тренера");
                        return;
                    }
                    viewModel.saveGroup(existing, text(name),
                            trainers.get(trainer.getSelectedItemPosition()).getId().toString(),
                            text(schedule), active.isChecked());
                }).show();
    }

    private void showClassDialog(GymClass existing) {
        if (groups.isEmpty() || trainers.isEmpty()) {
            toast("Сначала создайте активную группу и назначьте тренера");
            return;
        }
        LinearLayout content = dialogContent();
        Spinner group = spinner(groupLabels());
        Spinner trainer = spinner(trainerLabels());
        EditText start = field("Начало: 2026-06-12T18:00:00",
                existing == null ? null : existing.getScheduledStart());
        EditText end = field("Конец: 2026-06-12T19:00:00",
                existing == null ? null : existing.getScheduledEnd());
        EditText capacity = field("Вместимость",
                existing == null ? "20" : String.valueOf(existing.getMaxCapacity()));
        EditText location = field("Локация", existing == null ? "Main Box" : existing.getLocation());
        Spinner status = spinner(Arrays.asList("scheduled", "cancelled", "completed"));
        content.addView(group);
        content.addView(trainer);
        content.addView(start);
        content.addView(end);
        content.addView(capacity);
        content.addView(location);
        content.addView(status);
        if (existing != null) {
            selectGroup(group, existing.getGroupId());
            selectTrainer(trainer, existing.getTrainerId());
            status.setSelection(Math.max(Arrays.asList("scheduled", "cancelled", "completed")
                    .indexOf(existing.getStatus()), 0));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Новое занятие" : "Изменить занятие")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        Map<String, Object> fields = AdminRepository.classFields(
                                groups.get(group.getSelectedItemPosition()).getId(),
                                trainers.get(trainer.getSelectedItemPosition()).getId().toString(),
                                text(start), text(end), Integer.parseInt(text(capacity)),
                                text(location), String.valueOf(status.getSelectedItem())
                        );
                        viewModel.saveClass(existing, fields);
                    } catch (RuntimeException error) {
                        toast("Проверьте дату, время и вместимость");
                    }
                }).show();
    }

    private void showExerciseDialog(Exercise existing) {
        LinearLayout content = dialogContent();
        EditText name = field("Название упражнения", existing == null ? null : existing.getName());
        Spinner category = spinner(Arrays.asList("gymnastics", "weightlifting", "cardio", "monostructural"));
        EditText description = field("Описание", existing == null ? null : existing.getDescription());
        EditText unitType = field("Единица результата", existing == null ? "reps" : existing.getUnitType());
        EditText prUnit = field("Единица PR", existing == null ? null : existing.getPrUnit());
        Spinner direction = spinner(Arrays.asList("max", "min"));
        CheckBox active = checkbox("Упражнение активно", existing == null || existing.isActive());
        content.addView(name);
        content.addView(category);
        content.addView(description);
        content.addView(unitType);
        content.addView(prUnit);
        content.addView(direction);
        content.addView(active);
        if (existing != null) {
            selectValue(category, existing.getCategory());
            selectValue(direction, existing.getPrBetterDirection());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Новое упражнение" : "Изменить упражнение")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    if (text(name).isEmpty()) {
                        toast("Введите название упражнения");
                        return;
                    }
                    viewModel.saveExercise(existing, AdminRepository.exerciseFields(
                            text(name), String.valueOf(category.getSelectedItem()),
                            text(description), value(text(unitType), "reps"), text(prUnit),
                            String.valueOf(direction.getSelectedItem()), active.isChecked()
                    ));
                }).show();
    }

    private void showLoadTypeDialog(LoadType existing) {
        LinearLayout content = dialogContent();
        EditText code = field("Код", existing == null ? null : existing.getCode());
        EditText name = field("Название", existing == null ? null : existing.getName());
        EditText description = field("Описание", existing == null ? null : existing.getDescription());
        CheckBox active = checkbox("Тип нагрузки активен", existing == null || existing.isActive());
        content.addView(code);
        content.addView(name);
        content.addView(description);
        content.addView(active);

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Новый тип нагрузки" : "Изменить тип нагрузки")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String normalizedCode = normalizeCode(text(code));
                    if (normalizedCode.isEmpty() || text(name).isEmpty()) {
                        toast("Введите код и название");
                        return;
                    }
                    viewModel.saveLoadType(existing, AdminRepository.loadTypeFields(
                            normalizedCode, text(name), text(description), active.isChecked()
                    ));
                }).show();
    }

    private void showTrainingTaskDialog(TrainingTask existing) {
        if (exercises.isEmpty() || loadTypes.isEmpty()) {
            toast("Сначала добавьте упражнения и типы нагрузки");
            return;
        }
        LinearLayout content = dialogContent();
        EditText title = field("Название задания", existing == null ? null : existing.getTitle());
        Spinner exercise = spinner(exerciseLabels());
        Spinner loadType = spinner(loadTypeLabels());
        EditText rxLoad = field("RX нагрузка", existing == null ? null : existing.getRxLoadDescription());
        Spinner optionalExercise = spinner(optionalExerciseLabels());
        Spinner optionalLoadType = spinner(optionalLoadTypeLabels());
        EditText optionalLoad = field("Optional нагрузка",
                existing == null ? null : existing.getOptionalLoadDescription());
        EditText notes = field("Комментарий", existing == null ? null : existing.getNotes());
        CheckBox active = checkbox("Шаблон активен", existing == null || existing.isActive());
        content.addView(title);
        content.addView(exercise);
        content.addView(loadType);
        content.addView(rxLoad);
        content.addView(optionalExercise);
        content.addView(optionalLoadType);
        content.addView(optionalLoad);
        content.addView(notes);
        content.addView(active);
        if (existing != null) {
            selectExercise(exercise, existing.getRxExerciseId());
            selectLoadType(loadType, existing.getLoadTypeId());
            selectOptionalExercise(optionalExercise, existing.getOptionalExerciseId());
            selectOptionalLoadType(optionalLoadType, existing.getOptionalLoadTypeId());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Новый шаблон задания" : "Изменить шаблон задания")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    if (text(title).isEmpty() || text(rxLoad).isEmpty()) {
                        toast("Введите название и RX нагрузку");
                        return;
                    }
                    Exercise optionalExerciseItem = optionalExercise(optionalExercise);
                    LoadType optionalLoadTypeItem = optionalLoadType(optionalLoadType);
                    viewModel.saveTrainingTask(existing, AdminRepository.trainingTaskFields(
                            text(title),
                            exercises.get(exercise.getSelectedItemPosition()).getId(),
                            loadTypes.get(loadType.getSelectedItemPosition()).getId(),
                            text(rxLoad),
                            optionalExerciseItem == null ? null : optionalExerciseItem.getId(),
                            optionalLoadTypeItem == null ? null : optionalLoadTypeItem.getId(),
                            text(optionalLoad), text(notes), active.isChecked()
                    ));
                }).show();
    }

    private void showWodDialog(Wod wod) {
        LinearLayout content = dialogContent();
        EditText name = field("Название", wod.getName());
        EditText date = field("Дата YYYY-MM-DD", wod.getScheduledDate());
        EditText notes = field("Примечания", wod.getNotes());
        content.addView(name);
        content.addView(date);
        content.addView(notes);
        new AlertDialog.Builder(requireContext())
                .setTitle("Изменить WOD")
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> viewModel.saveWod(
                        wod, AdminRepository.wodFields(text(name), text(date), text(notes))
                )).show();
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

    private CheckBox checkbox(String label, boolean checked) {
        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setText(label);
        checkBox.setChecked(checked);
        return checkBox;
    }

    private Spinner spinner(List<String> values) {
        Spinner spinner = new Spinner(requireContext());
        spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, values));
        return spinner;
    }

    private List<String> trainerLabels() {
        List<String> labels = new ArrayList<>();
        for (User trainer : trainers) labels.add(trainer.getFullName());
        return labels;
    }

    private List<String> groupLabels() {
        List<String> labels = new ArrayList<>();
        for (Group group : groups) labels.add(group.getName());
        return labels;
    }

    private List<String> exerciseLabels() {
        List<String> labels = new ArrayList<>();
        for (Exercise exercise : exercises) labels.add(exercise.getName());
        return labels;
    }

    private List<String> loadTypeLabels() {
        List<String> labels = new ArrayList<>();
        for (LoadType loadType : loadTypes) labels.add(loadType.getName());
        return labels;
    }

    private List<String> optionalExerciseLabels() {
        List<String> labels = new ArrayList<>();
        labels.add(NO_OPTIONAL);
        labels.addAll(exerciseLabels());
        return labels;
    }

    private List<String> optionalLoadTypeLabels() {
        List<String> labels = new ArrayList<>();
        labels.add(NO_OPTIONAL);
        labels.addAll(loadTypeLabels());
        return labels;
    }

    private Exercise optionalExercise(Spinner spinner) {
        int index = spinner.getSelectedItemPosition() - 1;
        return index >= 0 && index < exercises.size() ? exercises.get(index) : null;
    }

    private LoadType optionalLoadType(Spinner spinner) {
        int index = spinner.getSelectedItemPosition() - 1;
        return index >= 0 && index < loadTypes.size() ? loadTypes.get(index) : null;
    }

    private void selectTrainer(Spinner spinner, String trainerId) {
        if (trainerId == null) return;
        for (int i = 0; i < trainers.size(); i++) {
            if (trainers.get(i).getId() != null
                    && trainerId.equals(trainers.get(i).getId().toString())) spinner.setSelection(i);
        }
    }

    private void selectGroup(Spinner spinner, String groupId) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId().equals(groupId)) spinner.setSelection(i);
        }
    }

    private void selectExercise(Spinner spinner, String exerciseId) {
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getId().equals(exerciseId)) spinner.setSelection(i);
        }
    }

    private void selectLoadType(Spinner spinner, String loadTypeId) {
        for (int i = 0; i < loadTypes.size(); i++) {
            if (loadTypes.get(i).getId().equals(loadTypeId)) spinner.setSelection(i);
        }
    }

    private void selectOptionalExercise(Spinner spinner, String exerciseId) {
        if (exerciseId == null) return;
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getId().equals(exerciseId)) spinner.setSelection(i + 1);
        }
    }

    private void selectOptionalLoadType(Spinner spinner, String loadTypeId) {
        if (loadTypeId == null) return;
        for (int i = 0; i < loadTypes.size(); i++) {
            if (loadTypes.get(i).getId().equals(loadTypeId)) spinner.setSelection(i + 1);
        }
    }

    private void selectValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(String.valueOf(spinner.getItemAtPosition(i)))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String nameForExercise(String id) {
        for (Exercise exercise : exercises) {
            if (exercise.getId().equals(id)) return exercise.getName();
        }
        return "упражнение";
    }

    private String nameForLoadType(String id) {
        for (LoadType loadType : loadTypes) {
            if (loadType.getId().equals(id)) return loadType.getName();
        }
        return "тип нагрузки";
    }

    private void confirm(String title, Runnable action) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) -> action.run())
                .show();
    }

    private String value(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String normalizeCode(String value) {
        return value.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private void toast(String value) {
        if (value != null) Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
