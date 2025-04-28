package com.example.doanmonhocltm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.R;
import com.example.doanmonhocltm.model.ViolationAll;
import com.example.doanmonhocltm.model.ViolationDetail;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViolationAllAdapter extends RecyclerView.Adapter<ViolationAllAdapter.ViolationViewHolder> {

    private List<ViolationAll> violations;
    private Context context;
    private OnViolationClickListener listener;

    public interface OnViolationClickListener {
        void onDetailClick(ViolationAll violation);
        void onResolveClick(ViolationAll violation);
    }

    public ViolationAllAdapter(Context context, List<ViolationAll> violations, OnViolationClickListener listener) {
        this.context = context;
        this.violations = violations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViolationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_violation, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationViewHolder holder, int position) {
        ViolationAll violation = violations.get(position);

        // Set violation ID
        holder.tvViolationId.setText("Biên bản #" + violation.getId());

        // Set status
        holder.tvStatus.setText(violation.isResolutionStatus() ? "Đã xử lý" : "Chưa xử lý");
        holder.tvStatus.setBackgroundResource(
                violation.isResolutionStatus() ? R.drawable.status_completed_bg : R.drawable.status_pending_bg);

        // Format and set time
        String formattedTime = formatDateTime(violation.getReportTime());
        holder.tvViolationTime.setText(formattedTime);

        // Set location
        holder.tvViolationLocation.setText(violation.getReportLocation());

        // Set penalty type
        holder.tvPenaltyType.setText(violation.getPenaltyType());

        // Format and set deadline
        String formattedDeadline = formatDate(violation.getResolutionDeadline());
        holder.tvDeadline.setText(formattedDeadline);

        // Clear previous violation details
        holder.llViolationDetails.removeAllViews();

        // Add each violation detail
        for (ViolationDetail detail : violation.getViolationDetails()) {
            View detailView = LayoutInflater.from(context).inflate(R.layout.item_violation_detail, null);
            TextView tvViolationType = detailView.findViewById(R.id.tvViolationType);
            TextView tvViolationFine = detailView.findViewById(R.id.tvViolationFine);

            tvViolationType.setText(detail.getViolationType());
            tvViolationFine.setText(formatCurrency(detail.getFineAmount()));

            holder.llViolationDetails.addView(detailView);
        }

        // Set total amount
        holder.tvTotalAmount.setText(formatCurrency(violation.getTotalFine()));

        // Set button listeners
        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(violation);
            }
        });

        holder.btnResolveViolation.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResolveClick(violation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return violations != null ? violations.size() : 0;
    }

    public void updateData(List<ViolationAll> newViolations) {
        this.violations = newViolations;
        notifyDataSetChanged();
    }

    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    private String formatDate(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    private String formatCurrency(float amount) {
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return currencyFormatter.format(amount) + " VNĐ";
    }

    static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView tvViolationId, tvStatus, tvViolationTime, tvViolationLocation;
        TextView tvPenaltyType, tvDeadline, tvTotalAmount;
        LinearLayout llViolationDetails;
        Button btnViewDetail, btnResolveViolation;

        public ViolationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvViolationId = itemView.findViewById(R.id.tvViolationId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvViolationTime = itemView.findViewById(R.id.tvViolationTime);
            tvViolationLocation = itemView.findViewById(R.id.tvViolationLocation);
            tvPenaltyType = itemView.findViewById(R.id.tvPenaltyType);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            llViolationDetails = itemView.findViewById(R.id.llViolationDetails);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnResolveViolation = itemView.findViewById(R.id.btnResolveViolation);
        }
    }
}