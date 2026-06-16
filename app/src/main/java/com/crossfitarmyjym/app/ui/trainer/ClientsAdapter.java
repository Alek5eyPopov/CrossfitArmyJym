package com.crossfitarmyjym.app.ui.trainer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crossfitarmyjym.app.data.model.User;
import com.crossfitarmyjym.app.databinding.ItemClientBinding;

import java.util.ArrayList;
import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {

    public interface ClientClickListener {
        void onClientClick(User user);
    }

    private List<User> clients = new ArrayList<>();
    private ClientClickListener clickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemClientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = clients.get(position);
        String name = user.getFullName();
        holder.binding.tvClientName.setText(name.isEmpty() ? user.getEmail() : name);
        holder.binding.tvClientEmail.setText(user.getEmail());
        holder.binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClientClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void submitList(List<User> newClients) {
        clients = newClients != null ? newClients : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setClientClickListener(ClientClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemClientBinding binding;

        ViewHolder(ItemClientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
