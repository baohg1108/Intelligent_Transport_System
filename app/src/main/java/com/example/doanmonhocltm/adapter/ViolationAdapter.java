package com.example.doanmonhocltm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.R;
import com.example.doanmonhocltm.model.Violation;

import java.util.List;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {
    private List<Violation> violations;
    private OnViolationDeleteListener deleteListener;

    // Interface for delete callback
    public interface OnViolationDeleteListener {
        void onViolationDelete(int position);
    }

    public ViolationAdapter(List<Violation> violations) {
        this.violations = violations;
    }

    public void setOnViolationDeleteListener(OnViolationDeleteListener listener) {
        this.deleteListener = listener;
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

        // Set delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onViolationDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }

    public static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFine;
        ImageButton btnDelete;

        public ViolationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvViolationName);
            tvFine = itemView.findViewById(R.id.tvViolationFine);
            btnDelete = itemView.findViewById(R.id.btnDeleteViolation);
        }
    }
}