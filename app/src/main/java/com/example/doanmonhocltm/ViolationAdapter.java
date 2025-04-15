package com.example.doanmonhocltm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.model.Violation;

import java.util.List;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {
    private List<Violation> violations;

    public ViolationAdapter(List<Violation> violations) {
        this.violations = violations;
    }

    @NonNull
    @Override
    public ViolationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_violation, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationViewHolder holder, int position) {
        Violation violation = violations.get(position);
        holder.tvName.setText(violation.getName());
        holder.tvFine.setText(String.format("%,d VNĐ", violation.getFineAmount()));
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }

    public static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFine;

        public ViolationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvViolationName);
            tvFine = itemView.findViewById(R.id.tvViolationFine);
        }
    }
}
