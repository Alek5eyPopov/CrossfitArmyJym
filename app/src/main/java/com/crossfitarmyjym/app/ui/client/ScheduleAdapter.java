package com.crossfitarmyjym.app.ui.client;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.ItemScheduleClassBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<GymClass> classes = new ArrayList<>();
    private Set<String> bookedClassIds = Collections.emptySet();
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBook(String classId);
    }

    public ScheduleAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemScheduleClassBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GymClass gymClass = classes.get(position);
        String location = gymClass.getLocation();
        holder.binding.tvClassName.setText(
                location == null || location.isEmpty() ? "CrossFit" : location);
        Date classDate = parseDate(gymClass.getScheduledStart());
        holder.binding.tvDateDay.setText(formatDate(classDate, "dd", "--"));
        holder.binding.tvDateMonth.setText(
                formatDate(classDate, "MMM", "---").replace(".", "").toUpperCase(Locale.getDefault()));
        holder.binding.tvClassTime.setText(formatDate(classDate, "HH:mm", "Время уточняется"));

        int available = Math.max(gymClass.getAvailableSlots(), 0);
        holder.binding.tvAvailableSlots.setText(
                holder.itemView.getContext().getString(R.string.slots_left, available));

        boolean booked = bookedClassIds.contains(gymClass.getId());
        holder.binding.btnBook.setText(booked ? R.string.already_booked : R.string.book);
        holder.binding.btnBook.setEnabled(!booked && available > 0);
        holder.binding.btnBook.setOnClickListener(v -> listener.onBook(gymClass.getId()));
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public void submitData(List<GymClass> newClasses, Set<String> newBookedClassIds) {
        classes = newClasses != null ? newClasses : new ArrayList<>();
        bookedClassIds = newBookedClassIds != null
                ? new HashSet<>(newBookedClassIds)
                : Collections.emptySet();
        notifyDataSetChanged();
    }

    public static String formatTime(String isoDate) {
        Date date = parseDate(isoDate);
        return formatDate(date, "dd.MM, HH:mm", isoDate == null ? "" : isoDate);
    }

    private static Date parseDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return null;
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(pattern, Locale.US);
                return input.parse(isoDate);
            } catch (Exception ignored) {
                // Try the next Supabase timestamp representation.
            }
        }
        return null;
    }

    private static String formatDate(Date date, String pattern, String fallback) {
        if (date == null) return fallback;
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    public static String formatDay(String isoDate) {
        return formatDate(parseDate(isoDate), "dd", "--");
    }

    public static String formatMonth(String isoDate) {
        return formatDate(parseDate(isoDate), "MMM", "---")
                .replace(".", "")
                .toUpperCase(Locale.getDefault());
    }

    public static String formatClock(String isoDate) {
        return formatDate(parseDate(isoDate), "HH:mm", "Время уточняется");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemScheduleClassBinding binding;

        ViewHolder(ItemScheduleClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
