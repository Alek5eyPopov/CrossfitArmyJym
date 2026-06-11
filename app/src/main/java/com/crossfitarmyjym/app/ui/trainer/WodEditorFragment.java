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

import com.crossfitarmyjym.app.databinding.FragmentWodEditorBinding;

import java.util.Objects;

/**
 * Фрагмент редактора WOD.
 * Позволяет тренеру создавать и редактировать тренировки дня.
 */
public class WodEditorFragment extends Fragment {

    private FragmentWodEditorBinding binding;
    private WodEditorViewModel viewModel;

    private static final String[] FORMATS = {"amrap", "emom", "for_time", "tabata", "ladder"};

    public static WodEditorFragment newInstance() {
        return new WodEditorFragment();
    }

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

        setupFormatSpinner();
        setupClickListeners();
        setupObservers();
    }

    private void setupFormatSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                FORMATS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFormat.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSaveWod.setOnClickListener(v -> attemptCreateWod());
    }

    private void setupObservers() {
        viewModel.getIsSaving().observe(getViewLifecycleOwner(), saving -> {
            binding.btnSaveWod.setEnabled(!saving);
            binding.progressBar.setVisibility(saving ? View.VISIBLE : View.GONE);
        });

        viewModel.getSaveResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptCreateWod() {
        String name = Objects.requireNonNull(binding.etWodName.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.etWodDescription.getText()).toString().trim();
        String format = binding.spinnerFormat.getSelectedItem() != null
                ? binding.spinnerFormat.getSelectedItem().toString()
                : "amrap";

        if (TextUtils.isEmpty(name)) {
            binding.tilWodName.setError("Введите название WOD");
            return;
        }
        binding.tilWodName.setError(null);

        viewModel.createWod(name, format, description, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}