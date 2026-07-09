package com.crossfitarmyjym.app.ui.news;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.NewsPost;
import com.crossfitarmyjym.app.databinding.ItemNewsPostBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final List<NewsPost> posts = new ArrayList<>();
    private final OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(NewsPost post);
    }

    public NewsAdapter(OnNewsClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NewsPost> value) {
        posts.clear();
        if (value != null) {
            posts.addAll(value);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNewsPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNewsPostBinding binding;

        ViewHolder(ItemNewsPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NewsPost post) {
            binding.tvNewsTitle.setText(post.getTitle());
            binding.tvNewsSummary.setText(summary(post));
            binding.tvNewsDate.setText(formatDate(post.getPublishedAt()));
            ImageLoader.load(binding.ivNewsImage, post.getImageUrl(), R.drawable.bg_army_hero);
            binding.getRoot().setOnClickListener(v -> listener.onNewsClick(post));
        }
    }

    private String summary(NewsPost post) {
        String value = post.getSummary();
        if (value == null || value.trim().isEmpty()) {
            value = post.getBody();
        }
        if (value == null) return "";
        return value.length() > 110 ? value.substring(0, 110).trim() + "..." : value;
    }

    public static String formatDate(String value) {
        Date date = parseDate(value);
        if (date == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date);
    }

    private static Date parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(value);
            } catch (Exception ignored) {
                // Try next format.
            }
        }
        return null;
    }
}
