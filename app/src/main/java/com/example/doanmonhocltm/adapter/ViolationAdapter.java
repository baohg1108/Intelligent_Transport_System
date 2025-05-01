package com.example.doanmonhocltm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.R;
import com.example.doanmonhocltm.model.ViolationItem;

import java.util.List;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {
    private List<ViolationItem> violationItems;
    private OnViolationDeleteListener deleteListener;

    // Interface for delete callback
    public interface OnViolationDeleteListener {
        void onViolationDelete(int position);
    }

    public ViolationAdapter(List<ViolationItem> violationItems) {
        this.violationItems = violationItems;
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
        ViolationItem violationItem = violationItems.get(position);
        holder.tvName.setText(violationItem.getName());
        holder.tvFine.setText(String.format("%,d VNĐ", violationItem.getFineAmount()));

        // Set delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onViolationDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return violationItems.size();
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