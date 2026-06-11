package com.crossfitarmyjym.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.data.model.Group;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.databinding.FragmentUsersBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private AdminUsersViewModel viewModel;
    private AdminUserAdapter adapter;
    private List<Group> groups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminUsersViewModel.class);
        adapter = new AdminUserAdapter(this::showEditDialog);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);
        binding.btnRefresh.setOnClickListener(v -> viewModel.load());
        observe();
        viewModel.load();
    }

    private void observe() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getGroups().observe(getViewLifecycleOwner(), value -> {
            groups = value != null ? value : new ArrayList<>();
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), value ->
                binding.progressBar.setVisibility(Boolean.TRUE.equals(value) ? View.VISIBLE : View.GONE));
        viewModel.getMessage().observe(getViewLifecycleOwner(), this::toast);
        viewModel.getError().observe(getViewLifecycleOwner(), this::toast);
    }

    private void showEditDialog(User user) {
        LinearLayout content = dialogContent();
        Spinner role = new Spinner(requireContext());
        role.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("athlete", "trainer", "admin")));
        role.setSelection(Math.max(Arrays.asList("athlete", "trainer", "admin").indexOf(user.getRole()), 0));
        content.addView(role);

        List<String> groupLabels = new ArrayList<>();
        groupLabels.add("Без группы");
        for (Group group : groups) groupLabels.add(group.getName());
        Spinner group = new Spinner(requireContext());
        group.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, groupLabels));
        if (user.getGroupId() != null) {
            for (int i = 0; i < groups.size(); i++) {
                if (user.getGroupId().toString().equals(groups.get(i).getId())) group.setSelection(i + 1);
            }
        }
        content.addView(group);
        CheckBox active = new CheckBox(requireContext());
        active.setText("Активный пользователь");
        active.setChecked(user.isActive());
        content.addView(active);

        new AlertDialog.Builder(requireContext())
                .setTitle(user.getFullName())
                .setView(content)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String groupId = group.getSelectedItemPosition() == 0
                            ? null : groups.get(group.getSelectedItemPosition() - 1).getId();
                    viewModel.update(user, String.valueOf(role.getSelectedItem()),
                            groupId, active.isChecked());
                })
                .show();
    }

    private LinearLayout dialogContent() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 0, padding, 0);
        return layout;
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
