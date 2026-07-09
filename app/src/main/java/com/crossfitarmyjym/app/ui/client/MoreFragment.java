package com.crossfitarmyjym.app.ui.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.databinding.FragmentClientPlaceholderBinding;

public class MoreFragment extends Fragment {

    private FragmentClientPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvPlaceholderTitle.setText(R.string.more_title);
        binding.tvPlaceholderMessage.setText(R.string.more_placeholder_message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
