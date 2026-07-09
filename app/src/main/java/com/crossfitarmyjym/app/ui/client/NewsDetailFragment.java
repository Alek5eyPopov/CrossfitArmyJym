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
import androidx.navigation.fragment.NavHostFragment;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.NewsPost;
import com.crossfitarmyjym.app.databinding.FragmentNewsDetailBinding;
import com.crossfitarmyjym.app.ui.news.ImageLoader;
import com.crossfitarmyjym.app.ui.news.NewsAdapter;
import com.crossfitarmyjym.app.ui.news.NewsViewModel;

public class NewsDetailFragment extends Fragment {

    private FragmentNewsDetailBinding binding;
    private NewsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewsDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        observe();
        String newsId = getArguments() == null ? null : getArguments().getString("news_id");
        if (newsId == null || newsId.isEmpty()) {
            Toast.makeText(requireContext(), "Новость не выбрана", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        viewModel.loadNewsPost(newsId);
    }

    private void observe() {
        viewModel.getSelectedPost().observe(getViewLifecycleOwner(), this::bind);
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void bind(NewsPost post) {
        if (post == null) return;
        binding.tvNewsTitle.setText(post.getTitle());
        binding.tvNewsDate.setText(NewsAdapter.formatDate(post.getPublishedAt()));
        binding.tvNewsBody.setText(post.getBody());
        ImageLoader.load(binding.ivHero, post.getImageUrl(), R.drawable.bg_army_hero);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
