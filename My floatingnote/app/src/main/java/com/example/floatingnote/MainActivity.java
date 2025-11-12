package com.example.floatingnote;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnSettings = findViewById(R.id.btn_settings);

        btnStart.setOnClickListener(v -> startFloatingNote());
        btnStop.setOnClickListener(v -> stopFloatingNote());
        btnSettings.setOnClickListener(v -> openSettings());
        
        checkOverlayPermission();
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission();
            }
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        }
    }

    private void startFloatingNote() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
            return;
        }
        
        Intent serviceIntent = new Intent(this, FloatingNoteService.class);
        startService(serviceIntent);
        Toast.makeText(this, "悬浮便签已启动", Toast.LENGTH_SHORT).show();
    }

    private void stopFloatingNote() {
        Intent serviceIntent = new Intent(this, FloatingNoteService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "悬浮便签已关闭", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
    // 打开设置界面
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "悬浮窗权限已获取", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
