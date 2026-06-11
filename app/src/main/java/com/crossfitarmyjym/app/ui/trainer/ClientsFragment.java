package com.crossfitarmyjym.app.ui.trainer;

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

import com.crossfitarmyjym.app.databinding.FragmentClientsBinding;

public class ClientsFragment extends Fragment {

    private FragmentClientsBinding binding;
    private ClientsViewModel viewModel;
    private ClientsAdapter adapter;

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
        binding.rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClients.setAdapter(adapter);

        viewModel.getClients().observe(getViewLifecycleOwner(), clients -> {
            adapter.submitList(clients);
            boolean empty = clients == null || clients.isEmpty();
            binding.rvClients.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.loadClients();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
