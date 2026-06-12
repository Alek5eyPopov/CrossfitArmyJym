package com.crossfitarmyjym.app.ui.trainer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.ItemTrainerClassBinding;
import com.crossfitarmyjym.app.ui.client.ScheduleAdapter;

import java.util.ArrayList;
import java.util.List;

public class TrainerClassesAdapter
        extends RecyclerView.Adapter<TrainerClassesAdapter.ViewHolder> {

    private List<GymClass> classes = new ArrayList<>();
    private final OnClassClickListener listener;

    public interface OnClassClickListener {
        void onClassClick(GymClass gymClass);
    }

    public TrainerClassesAdapter(OnClassClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTrainerClassBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GymClass gymClass = classes.get(position);
        String location = gymClass.getLocation();
        holder.binding.tvClassName.setText(
                location == null || location.isEmpty() ? "CrossFit" : location);
        holder.binding.tvDateDay.setText(ScheduleAdapter.formatDay(gymClass.getScheduledStart()));
        holder.binding.tvDateMonth.setText(ScheduleAdapter.formatMonth(gymClass.getScheduledStart()));
        holder.binding.tvClassTime.setText(ScheduleAdapter.formatClock(gymClass.getScheduledStart()));
        holder.binding.tvBookingsCount.setText(holder.itemView.getContext().getString(
                R.string.bookings_count,
                gymClass.getCurrentBookings(),
                gymClass.getMaxCapacity()));
        holder.binding.getRoot().setOnClickListener(v -> listener.onClassClick(gymClass));
        holder.binding.btnAttendance.setOnClickListener(v -> listener.onClassClick(gymClass));
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public void submitList(List<GymClass> newClasses) {
        classes = newClasses != null ? newClasses : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTrainerClassBinding binding;

        ViewHolder(ItemTrainerClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
