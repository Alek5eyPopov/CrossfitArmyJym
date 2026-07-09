package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.FragmentScheduleBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Фрагмент расписания занятий.
 * Отображает список доступных занятий с возможностью записи.
 */
public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private ScheduleViewModel viewModel;
    private ScheduleAdapter adapter;

    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        setupRecyclerView();
        setupObservers();
        binding.btnRefresh.setOnClickListener(v -> viewModel.refreshSchedule());
        binding.btnPrevWeek.setOnClickListener(v -> viewModel.shiftWeek(-1));
        binding.btnNextWeek.setOnClickListener(v -> viewModel.shiftWeek(1));

        viewModel.loadSchedule();
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(viewModel::bookClass);

        binding.rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSchedule.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getMonthTitle().observe(getViewLifecycleOwner(), month ->
                binding.tvMonth.setText(month == null ? "" : month.toUpperCase(Locale.getDefault())));

        viewModel.getVisibleDates().observe(getViewLifecycleOwner(), dates ->
                renderDateTabs(dates, viewModel.getSelectedDate().getValue()));

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), selected ->
                renderDateTabs(viewModel.getVisibleDates().getValue(), selected));

        viewModel.getClasses().observe(getViewLifecycleOwner(), gymClasses -> {
            if (gymClasses != null) {
                updateAdapter(gymClasses, viewModel.getBookedClassIds().getValue());
                boolean empty = gymClasses.isEmpty();
                binding.rvSchedule.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getBookedClassIds().observe(getViewLifecycleOwner(), bookedIds ->
                updateAdapter(viewModel.getClasses().getValue(), bookedIds));

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRefresh.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getBookingStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapter(List<GymClass> classes, Set<String> bookedIds) {
        adapter.submitData(
                classes != null ? classes : Collections.emptyList(),
                bookedIds != null ? bookedIds : Collections.emptySet());
    }

    private void renderDateTabs(List<Date> dates, Date selectedDate) {
        if (binding == null || dates == null) return;
        binding.dateTabs.removeAllViews();
        for (Date date : dates) {
            boolean selected = isSameDay(date, selectedDate);
            LinearLayout dayView = createDayView(date, selected);
            dayView.setOnClickListener(v -> viewModel.selectDate(date));
            binding.dateTabs.addView(dayView);
        }
    }

    private LinearLayout createDayView(Date date, boolean selected) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setBackgroundResource(selected
                ? R.drawable.bg_schedule_day_selected
                : R.drawable.bg_schedule_day_default);
        container.setClickable(true);
        container.setFocusable(true);
        int verticalPadding = dp(7);
        container.setPadding(0, verticalPadding, 0, verticalPadding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(dp(2), 0, dp(2), 0);
        container.setLayoutParams(params);

        TextView dayNumber = new TextView(requireContext());
        dayNumber.setText(formatDate(date, "d"));
        dayNumber.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.text_on_primary : R.color.text_primary));
        dayNumber.setTextSize(24);
        dayNumber.setTypeface(dayNumber.getTypeface(), android.graphics.Typeface.BOLD);
        dayNumber.setGravity(android.view.Gravity.CENTER);

        TextView dayName = new TextView(requireContext());
        dayName.setText(formatDate(date, "EE").replace(".", "").toUpperCase(Locale.getDefault()));
        dayName.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.text_on_primary : R.color.text_secondary));
        dayName.setTextSize(14);
        dayName.setGravity(android.view.Gravity.CENTER);

        container.addView(dayNumber);
        container.addView(dayName);
        return container;
    }

    private boolean isSameDay(Date first, Date second) {
        if (first == null || second == null) return false;
        Calendar firstCalendar = Calendar.getInstance();
        Calendar secondCalendar = Calendar.getInstance();
        firstCalendar.setTime(first);
        secondCalendar.setTime(second);
        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)
                && firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
