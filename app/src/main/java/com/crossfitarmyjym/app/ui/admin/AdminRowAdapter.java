package com.crossfitarmyjym.app.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.databinding.ItemAdminRowBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminRowAdapter<T> extends RecyclerView.Adapter<AdminRowAdapter<T>.ViewHolder> {

    public interface Presenter<T> {
        String title(T item);
        String subtitle(T item);
    }

    public interface Listener<T> {
        void onEdit(T item);
        void onDelete(T item);
    }

    private final Presenter<T> presenter;
    private final Listener<T> listener;
    private final List<T> items = new ArrayList<>();

    public AdminRowAdapter(Presenter<T> presenter, Listener<T> listener) {
        this.presenter = presenter;
        this.listener = listener;
    }

    public void submitList(List<T> value) {
        items.clear();
        if (value != null) items.addAll(value);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAdminRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminRowBinding binding;
        ViewHolder(ItemAdminRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(T item) {
            binding.tvTitle.setText(presenter.title(item));
            binding.tvSubtitle.setText(presenter.subtitle(item));
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }
    }
}
