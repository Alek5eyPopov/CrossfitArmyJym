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
import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.repository.AdminRepository;
import com.crossfitarmyjym.app.databinding.FragmentContentBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContentFragment extends Fragment {

    private FragmentContentBinding binding;
    private AdminContentViewModel viewModel;
    private List<Group> groups = new ArrayList<>();
    private List<User> trainers = new ArrayList<>();

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
        binding.btnCreateWod.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_content_to_wod_editor));
        observe();
        viewModel.load();
    }

    private void setupLists() {
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
        CheckBox active = new CheckBox(requireContext());
        active.setText("Группа активна");
        active.setChecked(existing == null || existing.isActive());
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
                    if (trainers.isEmpty() || name.getText().toString().trim().isEmpty()) {
                        toast("Укажите название и добавьте хотя бы одного тренера");
                        return;
                    }
                    viewModel.saveGroup(existing, name.getText().toString().trim(),
                            trainers.get(trainer.getSelectedItemPosition()).getId().toString(),
                            schedule.getText().toString().trim(), active.isChecked());
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
                                start.getText().toString().trim(), end.getText().toString().trim(),
                                Integer.parseInt(capacity.getText().toString()),
                                location.getText().toString().trim(),
                                String.valueOf(status.getSelectedItem())
                        );
                        viewModel.saveClass(existing, fields);
                    } catch (RuntimeException error) {
                        toast("Проверьте дату, время и вместимость");
                    }
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
                        wod, AdminRepository.wodFields(name.getText().toString().trim(),
                                date.getText().toString().trim(), notes.getText().toString().trim())
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

    private void toast(String value) {
        if (value != null) Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
