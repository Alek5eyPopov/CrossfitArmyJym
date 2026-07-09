package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.preferences.PreferencesManager;
import com.crossfitarmyjym.app.databinding.FragmentClientPlaceholderBinding;
import com.google.android.material.button.MaterialButton;

public class MoreFragment extends Fragment {

    private FragmentClientPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvPlaceholderTitle.setText(R.string.more_title);
        binding.tvPlaceholderMessage.setText(R.string.more_placeholder_message);
        renderRoleActions();
    }

    private void renderRoleActions() {
        binding.actionsContainer.removeAllViews();
        String role = PreferencesManager.getInstance().getUserRole();
        if ("trainer".equals(role)) {
            binding.tvPlaceholderMessage.setText("Дополнительные инструменты тренера");
            addAction("WOD редактор", R.id.fragment_wod_editor);
            addAction("Атлеты", R.id.fragment_clients);
        } else if ("admin".equals(role)) {
            binding.tvPlaceholderMessage.setText("Дополнительные инструменты администратора");
            addAction("Управление залом", R.id.fragment_content);
            addAction("Статистика", R.id.fragment_statistics);
        }
    }

    private void addAction(String label, int destinationId) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(destinationId));
        binding.actionsContainer.addView(button);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
