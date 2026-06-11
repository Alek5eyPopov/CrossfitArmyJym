package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crossfitarmyjym.app.data.model.LeaderboardEntry;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.data.model.WodExercise;
import com.crossfitarmyjym.app.databinding.FragmentHomeClientBinding;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeClientBinding binding;
    private HomeViewModel viewModel;

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
        binding.btnSubmitResult.setOnClickListener(v -> showResultDialog());
        binding.btnLeaderboard.setOnClickListener(v -> viewModel.loadLeaderboard());
        observe();
        viewModel.loadHomeData();
    }

    private void observe() {
        viewModel.getTodaysWod().observe(getViewLifecycleOwner(), wod -> {
            binding.tvWodContent.setText(wod == null
                    ? "WOD на сегодня не назначен" : formatWod(wod));
            binding.btnSubmitResult.setEnabled(wod != null);
            binding.btnLeaderboard.setEnabled(wod != null);
        });
        viewModel.getLeaderboard().observe(getViewLifecycleOwner(), this::showLeaderboard);
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private String formatWod(Wod wod) {
        StringBuilder text = new StringBuilder(wod.getName())
                .append("\nФормат: ").append(wod.getFormat());
        if (wod.getTimeCapSeconds() > 0) {
            text.append("\nЛимит: ").append(wod.getTimeCapSeconds() / 60).append(" мин");
        }
        List<WodExercise> exercises = wod.getExercises();
        if (exercises != null) {
            for (WodExercise item : exercises) {
                if (item.getExercise() == null) continue;
                text.append("\n• ").append(item.getExercise().getName())
                        .append(" — ").append(item.getRounds()).append(" раунд.");
                if (item.getRecommendedWeightKg() > 0) {
                    text.append(", ").append(item.getRecommendedWeightKg()).append(" кг");
                }
            }
        }
        if (wod.getNotes() != null && !wod.getNotes().isEmpty()) {
            text.append("\n\n").append(wod.getNotes());
        }
        return text.toString();
    }

    private void showResultDialog() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 0, padding, 0);
        EditText scoreInput = new EditText(requireContext());
        scoreInput.setHint("Числовой результат");
        scoreInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        container.addView(scoreInput);
        EditText displayInput = new EditText(requireContext());
        displayInput.setHint("Например 12:45 или 120 reps");
        container.addView(displayInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Результат WOD")
                .setView(container)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    try {
                        double score = Double.parseDouble(scoreInput.getText().toString());
                        if (score < 0) throw new NumberFormatException();
                        viewModel.submitResult(score, displayInput.getText().toString().trim());
                        dialog.dismiss();
                    } catch (NumberFormatException error) {
                        scoreInput.setError("Введите число не меньше нуля");
                    }
                }));
        dialog.show();
    }

    private void showLeaderboard(List<LeaderboardEntry> entries) {
        StringBuilder text = new StringBuilder();
        if (entries == null || entries.isEmpty()) {
            text.append("Результатов пока нет");
        } else {
            for (LeaderboardEntry entry : entries) {
                text.append(String.format(Locale.getDefault(), "%d. %s — %s%s\n",
                        entry.getRank(), entry.getFullName(), entry.getFormattedScore(),
                        entry.isPr() ? "  PR" : ""));
            }
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Рейтинг WOD")
                .setMessage(text.toString().trim())
                .setPositiveButton("Закрыть", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
