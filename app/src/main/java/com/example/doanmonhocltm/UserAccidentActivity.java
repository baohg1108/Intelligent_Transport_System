package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import com.example.doanmonhocltm.InformationActivity;
import com.example.doanmonhocltm.PoliceNoAccidentActivity;
import com.example.doanmonhocltm.SettingActivity;

public class UserAccidentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accident);

        //get id from .xml
        Button btnCallCSGT = findViewById(R.id.btnCallCSGT);
        Button btnSendSignalCSGT = findViewById(R.id.btnSendSignalCSGT);

        // nút gọi csgt
        btnCallCSGT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // list phone number - thường là của mấy ô tổ trưởng lưu trong database sẵn
                final String[] csgtNumbers = new String[]{"113 - Cảnh sát 113", "114 - Cảnh sát PCCC", "115 - Cấp cứu y tế"};

                // create  AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(UserAccidentActivity.this);
                builder.setTitle("Gọi hỗ trợ");
                builder.setItems(csgtNumbers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Xử lý khi người dùng chọn một số
                        String selectedNumber = csgtNumbers[which];
                        // Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "113"));
                        // startActivity(callIntent);
                    }
                });

                // díplay dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // next page information
        btnSendSignalCSGT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //logic gửi tín hiệu tới csgt

                // tạo AlertDialog để thông báo kết quả
                AlertDialog.Builder builder = new AlertDialog.Builder(UserAccidentActivity.this);
                builder.setTitle("Thông báo");
                builder.setMessage("Đã gửi tín hiệu thành công đến CSGT!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // hiển thị dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



    }

}