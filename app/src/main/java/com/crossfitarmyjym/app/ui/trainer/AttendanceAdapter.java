package com.crossfitarmyjym.app.ui.trainer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.data.model.AttendanceEntry;
import com.crossfitarmyjym.app.databinding.ItemAttendanceBinding;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceEntry> entries = new ArrayList<>();
    private final OnAttendanceChangedListener listener;

    public interface OnAttendanceChangedListener {
        void onChanged(String userId, boolean attended);
    }

    public AttendanceAdapter(OnAttendanceChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAttendanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceEntry entry = entries.get(position);
        String name = entry.getFullName();
        String email = entry.getEmail();
        String displayName = name;
        if (displayName == null || displayName.isEmpty()) {
            displayName = email;
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = entry.getUserId();
        }
        holder.binding.tvClientName.setText(displayName);
        holder.binding.tvClientEmail.setText(entry.getEmail());
        holder.binding.cbAttended.setOnCheckedChangeListener(null);
        holder.binding.cbAttended.setChecked(entry.isAttended());
        holder.binding.cbAttended.setOnCheckedChangeListener((button, checked) -> {
            entry.setAttended(checked);
            listener.onChanged(entry.getUserId(), checked);
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void submitList(List<AttendanceEntry> newEntries) {
        entries = newEntries != null ? newEntries : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAttendanceBinding binding;

        ViewHolder(ItemAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
