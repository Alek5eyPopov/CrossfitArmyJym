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

import com.crossfitarmyjym.app.databinding.FragmentMyClassesBinding;

/**
 * Фрагмент отображения списка занятий тренера.
 * При клике на занятие открывается список записанных клиентов.
 */
public class TrainerClassesFragment extends Fragment {

    private FragmentMyClassesBinding binding;
    private TrainerClassesViewModel viewModel;

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

        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupObservers();

        viewModel.loadMyClasses();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: показать/скрыть ProgressBar
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getClasses().observe(getViewLifecycleOwner(), classes -> {
            if (classes != null && !classes.isEmpty()) {
                // TODO: установить адаптер
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}