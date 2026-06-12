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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crossfitarmyjym.app.databinding.FragmentBookingsBinding;

public class BookingsFragment extends Fragment {

    private FragmentBookingsBinding binding;
    private BookingsViewModel viewModel;
    private BookingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BookingsViewModel.class);
        adapter = new BookingsAdapter(viewModel::cancelBooking);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(adapter);
        binding.btnRefresh.setOnClickListener(v -> viewModel.refreshMyBookings());

        viewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            adapter.submitList(bookings);
            boolean empty = bookings == null || bookings.isEmpty();
            binding.rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRefresh.setEnabled(!loading);
        });
        viewModel.getCancelStatus().observe(getViewLifecycleOwner(), status -> showMessage(status));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> showMessage(error));

        viewModel.loadMyBookings();
    }

    private void showMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
