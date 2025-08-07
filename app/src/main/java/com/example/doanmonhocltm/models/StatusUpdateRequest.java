// gửi trạng thái lên server

package com.example.doanmonhocltm.models;

import com.google.gson.annotations.SerializedName;

public class StatusUpdateRequest {
    @SerializedName("status")
    private String status;

    public StatusUpdateRequest(String status) {
        this.status = status;
    }
}

