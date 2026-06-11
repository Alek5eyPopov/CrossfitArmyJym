package com.crossfitarmyjym.app.ui.client;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.ItemScheduleClassBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для отображения списка занятий в расписании.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<GymClass> classes;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBook(String classId);
    }

    public ScheduleAdapter(List<GymClass> classes, OnBookClickListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleClassBinding binding = ItemScheduleClassBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GymClass gymClass = classes.get(position);

        holder.binding.tvClassName.setText(gymClass.getLocation() + " Box");
        holder.binding.tvClassTime.setText(formatTime(gymClass.getScheduledStart()));

        int available = gymClass.getAvailableSlots();
        holder.binding.tvAvailableSlots.setText(available + " мест");

        holder.binding.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBook(gymClass.getId());
            }
        });

        // Отключаем кнопку если нет мест
        holder.binding.btnBook.setEnabled(available > 0);
    }

    @Override
    public int getItemCount() {
        return classes != null ? classes.size() : 0;
    }

    public void updateData(List<GymClass> newClasses) {
        this.classes = newClasses;
        notifyDataSetChanged();
    }

    private String formatTime(String isoDate) {
        // Простое форматирование: yyyy-MM-dd HH:mm
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            return output.format(input.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemScheduleClassBinding binding;

        ViewHolder(ItemScheduleClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}