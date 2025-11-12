package com.example.floatingnote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private SeekBar transparencySeekBar;
    private SeekBar textSizeSeekBar;
    private TextView previewText;
    private TextView transparencyText;
    private TextView textSizeText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("FloatingNotePrefs", MODE_PRIVATE);
        
        // 初始化视图
        transparencySeekBar = findViewById(R.id.seekbar_transparency);
        textSizeSeekBar = findViewById(R.id.seekbar_text_size);
        previewText = findViewById(R.id.tv_preview);
        transparencyText = findViewById(R.id.seekbar_transparency_text);
        textSizeText = findViewById(R.id.seekbar_text_size_text);
        Button saveButton = findViewById(R.id.btn_save);

        setupSeekBars();
        loadSettings();
        
        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }

    private void setupSeekBars() {
        transparencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transparencyText.setText("当前透明度: " + progress + "%");
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int textSize = 12 + progress;
                textSizeText.setText("当前字体大小: " + textSize + "sp");
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });
    }

    private void updatePreview() {
        float alpha = transparencySeekBar.getProgress() / 100f;
        float textSize = 12 + textSizeSeekBar.getProgress();
        
        previewText.setAlpha(alpha);
        previewText.setTextSize(textSize);
    }

    private void loadSettings() {
        int transparency = preferences.getInt("transparency", 80);
        int textSizeProgress = preferences.getInt("text_size", 14) - 12;
        
        transparencySeekBar.setProgress(transparency);
        textSizeSeekBar.setProgress(textSizeProgress);
        
        transparencyText.setText("当前透明度: " + transparency + "%");
        textSizeText.setText("当前字体大小: " + (12 + textSizeProgress) + "sp");
        
        updatePreview();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("transparency", transparencySeekBar.getProgress());
        editor.putInt("text_size", 12 + textSizeSeekBar.getProgress());
        editor.apply();
    }
}
