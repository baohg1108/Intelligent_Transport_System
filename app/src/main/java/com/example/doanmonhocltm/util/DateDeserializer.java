// Fix lỗi vì bên backend bỏ BEAN
package com.example.doanmonhocltm.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DateDeserializer extends TypeAdapter<Long> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public void write(JsonWriter out, Long value) throws IOException {
        out.value(value); // Nếu cần serialize ngược (long -> JSON)
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        String dateStr = in.nextString(); // Đọc chuỗi từ JSON
        OffsetDateTime dateTime = OffsetDateTime.parse(dateStr, formatter); // Chuyển sang OffsetDateTime
        return dateTime.toInstant().toEpochMilli(); // Lấy timestamp (milliseconds)
    }
}
