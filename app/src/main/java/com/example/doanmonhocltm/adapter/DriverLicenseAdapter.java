package com.example.doanmonhocltm.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import com.example.doanmonhocltm.model.DriverLicense;
import com.example.doanmonhocltm.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DriverLicenseAdapter extends RecyclerView.Adapter<DriverLicenseAdapter.DriverLicenseViewHolder> {
    private List<DriverLicense> driverLicenses;  // Danh sách các đối tượng DriverLicense

    // Constructor
    public DriverLicenseAdapter() {
        this.driverLicenses = new ArrayList<>();
    }

    // Constructor với danh sách ban đầu
    public DriverLicenseAdapter(List<DriverLicense> driverLicenses) {
        this.driverLicenses = driverLicenses;
    }

    // Phương thức cập nhật dữ liệu
    public void setDriverLicenses(List<DriverLicense> driverLicenses) {
        this.driverLicenses = driverLicenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DriverLicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_license, parent, false);
        return new DriverLicenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverLicenseViewHolder holder, int position) {
        DriverLicense driverLicenseItem = driverLicenses.get(position);
        holder.tvLicenseNumber.setText(driverLicenseItem.getLicenseNumber());
        holder.tvLicenseClass.setText(driverLicenseItem.getLicenseClass());
        holder.tvPlaceOfIssue.setText(driverLicenseItem.getPlaceOfIssue());
        holder.tvIssueDate.setText(driverLicenseItem.getIssueDate());
        holder.tvExpiryDate.setText(driverLicenseItem.getExpiryDate());
    }

    @Override
    public int getItemCount() {
        return driverLicenses != null ? driverLicenses.size() : 0;
    }

    public static class DriverLicenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvLicenseNumber, tvLicenseClass, tvPlaceOfIssue, tvIssueDate, tvExpiryDate;

        public DriverLicenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLicenseNumber = itemView.findViewById(R.id.tvLicenseNumber);
            tvLicenseClass = itemView.findViewById(R.id.tvLicenseClass);
            tvPlaceOfIssue = itemView.findViewById(R.id.tvPlaceOfIssue);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
        }
    }
}