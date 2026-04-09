package com.example.civsetu.sustainability;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.civsetu.R;
import com.example.civsetu.databinding.ItemLeaderboardUserBinding;
import com.example.civsetu.model.User;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<User> leaderboard;

    public LeaderboardAdapter(List<User> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void setLeaderboard(List<User> leaderboard) {
        this.leaderboard = leaderboard;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardUserBinding binding = ItemLeaderboardUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = leaderboard.get(position);
        int rank = position + 1;
        holder.binding.tvRank.setText(String.valueOf(rank));
        holder.binding.tvUserName.setText(user.getName());
        holder.binding.tvPoints.setText(String.valueOf(user.getImpactPoints()));

        // Ranking Colors
        int color;
        switch (rank) {
            case 1: color = holder.itemView.getContext().getColor(R.color.rank_1); break;
            case 2: color = holder.itemView.getContext().getColor(R.color.rank_2); break;
            case 3: color = holder.itemView.getContext().getColor(R.color.rank_3); break;
            default: color = holder.itemView.getContext().getColor(R.color.rank_other); break;
        }
        holder.binding.cardRank.setCardBackgroundColor(ColorStateList.valueOf(color));
        
        // Badge Logic (Mock for now or based on points)
        if (user.getImpactPoints() > 2000) holder.binding.tvBadge.setText("Eco Guardian");
        else if (user.getImpactPoints() > 1000) holder.binding.tvBadge.setText("Cleanliness Crusader");
        else holder.binding.tvBadge.setText("Civic Participant");
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderboardUserBinding binding;
        ViewHolder(ItemLeaderboardUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
