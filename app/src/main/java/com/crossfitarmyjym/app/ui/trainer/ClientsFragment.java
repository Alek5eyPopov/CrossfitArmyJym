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

import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.databinding.FragmentClientsBinding;
import com.crossfitarmyjym.app.ui.progress.AthleteProgressDialog;

public class ClientsFragment extends Fragment {

    private FragmentClientsBinding binding;
    private ClientsViewModel viewModel;
    private ClientsAdapter adapter;
    private AthleteProgressDialog progressDialog;

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
            if (Boolean.TRUE.equals(loading) && progressDialog != null) {
                progressDialog.setLoading();
            }
        });
        viewModel.getSelectedClientBests().observe(getViewLifecycleOwner(), records -> {
            if (progressDialog != null) {
                progressDialog.setBests(records);
            }
        });
        viewModel.getSelectedClientPrHistory().observe(getViewLifecycleOwner(), records -> {
            if (progressDialog != null) {
                progressDialog.setHistory(records);
            }
        });
        viewModel.getSelectedClientWodResults().observe(getViewLifecycleOwner(), results -> {
            if (progressDialog != null) {
                progressDialog.setWodResults(results);
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
        progressDialog = new AthleteProgressDialog(requireContext(), title);
        progressDialog.setOnDismissListener(dialog -> {
            progressDialog = null;
        });
        progressDialog.show();
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
