package com.crossfitarmyjym.app.ui.client;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.Booking;
import com.crossfitarmyjym.app.data.model.GymClass;
import com.crossfitarmyjym.app.databinding.ItemBookingBinding;

import java.util.ArrayList;
import java.util.List;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private final OnCancelClickListener listener;

    public interface OnCancelClickListener {
        void onCancel(String bookingId);
    }

    public BookingsAdapter(OnCancelClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        GymClass gymClass = booking.getGymClass();
        String location = gymClass != null ? gymClass.getLocation() : null;
        holder.binding.tvClassName.setText(
                location == null || location.isEmpty() ? "CrossFit" : location);
        holder.binding.tvClassTime.setText(
                gymClass != null
                        ? ScheduleAdapter.formatTime(gymClass.getScheduledStart())
                        : holder.itemView.getContext().getString(R.string.class_details_unavailable));

        boolean confirmed = booking.isConfirmed();
        holder.binding.tvBookingStatus.setText(
                confirmed ? R.string.booking_confirmed : R.string.booking_cancelled);
        holder.binding.btnCancel.setEnabled(confirmed);
        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancel(booking.getId()));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void submitList(List<Booking> newBookings) {
        bookings = newBookings != null ? newBookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemBookingBinding binding;

        ViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
