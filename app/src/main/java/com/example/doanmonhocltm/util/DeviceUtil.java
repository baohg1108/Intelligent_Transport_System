package com.example.doanmonhocltm.util;


import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceUtil {

    // Lấy địa chỉ IP của thiết bị
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4) return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // bỏ hậu tố vùng trong IPv6
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    // Lấy thông tin thiết bị
    public static String getDeviceInfo() {
        return "Manufacturer: " + Build.MANUFACTURER +
                ", Model: " + Build.MODEL +
                ", Device: " + Build.DEVICE +
                ", Brand: " + Build.BRAND +
                ", Product: " + Build.PRODUCT +
                ", SDK: " + Build.VERSION.SDK_INT +
                ", Release: " + Build.VERSION.RELEASE;
    }
}
