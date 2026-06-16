package com.crossfitarmyjym.app.ui.trainer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.data.model.Wod;
import com.crossfitarmyjym.app.databinding.FragmentClientsBinding;

import java.util.List;
import java.util.Locale;

public class ClientsFragment extends Fragment {

    private FragmentClientsBinding binding;
    private ClientsViewModel viewModel;
    private ClientsAdapter adapter;
    private AlertDialog progressDialog;
    private TextView progressBestsView;
    private TextView progressHistoryView;
    private TextView progressWodResultsView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ClientsViewModel.class);
        adapter = new ClientsAdapter();
        adapter.setClientClickListener(client -> {
            showClientProgressDialog(client);
            viewModel.loadClientProgress(client);
        });
        binding.rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClients.setAdapter(adapter);

        viewModel.getClients().observe(getViewLifecycleOwner(), clients -> {
            adapter.submitList(clients);
            boolean empty = clients == null || clients.isEmpty();
            binding.rvClients.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getProgressLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                setProgressDialogLoading();
            }
        });
        viewModel.getSelectedClientBests().observe(getViewLifecycleOwner(), records -> {
            if (progressBestsView != null) {
                progressBestsView.setText(formatPersonalRecords(records, getString(R.string.no_pr_records)));
            }
        });
        viewModel.getSelectedClientPrHistory().observe(getViewLifecycleOwner(), records -> {
            if (progressHistoryView != null) {
                progressHistoryView.setText(formatPersonalRecords(records, getString(R.string.no_pr_records)));
            }
        });
        viewModel.getSelectedClientWodResults().observe(getViewLifecycleOwner(), results -> {
            if (progressWodResultsView != null) {
                progressWodResultsView.setText(formatWodResults(results));
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.loadClients();
    }

    private void showClientProgressDialog(@NonNull User client) {
        String title = client.getFullName().isEmpty() ? client.getEmail() : client.getFullName();

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (18 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, 0);
        scrollView.addView(content);

        progressBestsView = addProgressSection(content, getString(R.string.client_progress_bests));
        progressHistoryView = addProgressSection(content, getString(R.string.client_progress_pr_history));
        progressWodResultsView = addProgressSection(content, getString(R.string.client_progress_wod_results));
        setProgressDialogLoading();

        progressDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        progressDialog.setOnDismissListener(dialog -> {
            progressDialog = null;
            progressBestsView = null;
            progressHistoryView = null;
            progressWodResultsView = null;
        });
        progressDialog.show();
    }

    private TextView addProgressSection(@NonNull LinearLayout parent, @NonNull String title) {
        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.army_navy));
        titleView.setTextSize(17);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, 0);
        parent.addView(titleView);

        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                (int) (48 * getResources().getDisplayMetrics().density),
                (int) (3 * getResources().getDisplayMetrics().density)
        );
        dividerParams.topMargin = (int) (7 * getResources().getDisplayMetrics().density);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.army_red));
        parent.addView(divider);

        TextView bodyView = new TextView(requireContext());
        bodyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        bodyView.setTextSize(14);
        bodyView.setLineSpacing(4, 1);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bodyParams.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
        bodyView.setLayoutParams(bodyParams);
        parent.addView(bodyView);
        return bodyView;
    }

    private void setProgressDialogLoading() {
        if (progressBestsView != null) {
            progressBestsView.setText(R.string.loading);
        }
        if (progressHistoryView != null) {
            progressHistoryView.setText(R.string.loading);
        }
        if (progressWodResultsView != null) {
            progressWodResultsView.setText(R.string.loading);
        }
    }

    private String formatPersonalRecords(List<PersonalRecord> records, String emptyText) {
        if (records == null || records.isEmpty()) {
            return emptyText;
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(records.size(), 8);
        for (int i = 0; i < limit; i++) {
            PersonalRecord record = records.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append("• ")
                    .append(resolveExerciseName(record))
                    .append(" - ")
                    .append(nonEmpty(record.getResultText(), formatResultValue(record)));
            String achievedAt = displayDate(record.getAchievedAt());
            if (!achievedAt.isEmpty()) {
                builder.append(" · ").append(achievedAt);
            }
        }
        if (records.size() > limit) {
            builder.append("\n+").append(records.size() - limit).append(" еще");
        }
        return builder.toString();
    }

    private String formatWodResults(List<Result> results) {
        if (results == null || results.isEmpty()) {
            return getString(R.string.no_wod_results);
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(results.size(), 8);
        for (int i = 0; i < limit; i++) {
            Result result = results.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            Wod wod = result.getWod();
            String wodName = wod != null ? wod.getName() : null;
            builder.append("• ")
                    .append(nonEmpty(wodName, "WOD"))
                    .append(" - ")
                    .append(nonEmpty(result.getFormattedScore(), String.valueOf(result.getScore())));
            String completedAt = displayDate(result.getCompletedAt());
            if (!completedAt.isEmpty()) {
                builder.append(" · ").append(completedAt);
            }
        }
        if (results.size() > limit) {
            builder.append("\n+").append(results.size() - limit).append(" еще");
        }
        return builder.toString();
    }

    private String resolveExerciseName(PersonalRecord record) {
        if (record.getExerciseName() != null && !record.getExerciseName().trim().isEmpty()) {
            return record.getExerciseName();
        }
        if (record.getExercise() != null && record.getExercise().getName() != null
                && !record.getExercise().getName().trim().isEmpty()) {
            return record.getExercise().getName();
        }
        return "Упражнение";
    }

    private String formatResultValue(PersonalRecord record) {
        if (record.getResultValue() == null) {
            return "";
        }
        String value = String.format(Locale.getDefault(), "%.2f", record.getResultValue())
                .replaceAll("[,.]00$", "");
        if (record.getUnit() != null && !record.getUnit().trim().isEmpty()) {
            return value + " " + record.getUnit().trim();
        }
        return value;
    }

    private String displayDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        int dateLength = Math.min(10, value.length());
        return value.substring(0, dateLength);
    }

    private String nonEmpty(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}
