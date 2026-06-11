package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.databinding.FragmentHomeClientBinding;

/**
 * Фрагмент главной страницы клиента.
 * Отображает WOD дня, ближайшие занятия и быстрые действия.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeClientBinding binding;
    private HomeViewModel viewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupObservers();

        viewModel.loadHomeData();
    }

    private void setupObservers() {
        // WOD дня
        viewModel.getTodaysWod().observe(getViewLifecycleOwner(), wod -> {
            if (wod != null) {
                String wodText = wod.getName() + "\n" +
                        "Формат: " + wod.getFormat() + "\n" +
                        (wod.getNotes() != null ? wod.getNotes() : "");
                binding.tvWodContent.setText(wodText);
            } else {
                binding.tvWodContent.setText("WOD на сегодня не назначен");
            }
        });

        // Загрузка
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Можно добавить отображение ProgressBar
        });

        // Ошибки
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}