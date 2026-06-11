package com.crossfitarmyjym.app.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.databinding.ItemAdminUserBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(User user);
    }

    private final Listener listener;
    private final List<User> users = new ArrayList<>();

    public AdminUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<User> value) {
        users.clear();
        if (value != null) users.addAll(value);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding binding;

        ViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvName.setText(user.getFullName());
            binding.tvEmail.setText(user.getEmail());
            binding.tvRole.setText(user.getRole() + (user.isActive() ? " • активен" : " • заблокирован"));
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        }
    }
}
