package com.example.floatingnote;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FloatingNoteService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private EditText noteEditText;
    private InputMethodManager inputMethodManager;
    private ClipboardManager clipboardManager;
    
    private LinearLayout textMenuLayout;
    private ImageButton btnTextMenu;
    private Button btnSelectAll, btnCopy, btnCut, btnPaste;

    @Override
    public void onCreate() {
        super.onCreate();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        createFloatingWindow();
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                   WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                   WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    flags,
                    PixelFormat.TRANSLUCENT
            );
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    flags,
                    PixelFormat.TRANSLUCENT
            );
        }
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;

        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.floating_note_layout, null);

        // 初始化视图
        initViews();
        setupClickListeners();
        setupTextSelection();

        windowManager.addView(floatingView, params);
    }

    private void initViews() {
        noteEditText = floatingView.findViewById(R.id.et_note);
        textMenuLayout = floatingView.findViewById(R.id.text_menu_layout);
        btnTextMenu = floatingView.findViewById(R.id.btn_text_menu);
        btnSelectAll = floatingView.findViewById(R.id.btn_select_all);
        btnCopy = floatingView.findViewById(R.id.btn_copy);
        btnCut = floatingView.findViewById(R.id.btn_cut);
        btnPaste = floatingView.findViewById(R.id.btn_paste);
        
        ImageButton btnClose = floatingView.findViewById(R.id.btn_close);
        ImageButton btnMinimize = floatingView.findViewById(R.id.btn_minimize);
        ImageButton btnSettings = floatingView.findViewById(R.id.btn_settings);
        LinearLayout headerLayout = floatingView.findViewById(R.id.header_layout);

        // 设置拖动功能
        setupDragFunctionality(headerLayout);
        
        btnClose.setOnClickListener(v -> stopSelf());
        btnMinimize.setOnClickListener(v -> minimizeNote());
        btnSettings.setOnClickListener(v -> showSettingsPopup());
    }

    private void setupClickListeners() {
        // 文本菜单按钮
        btnTextMenu.setOnClickListener(v -> toggleTextMenu());
        
        // 文本操作按钮
        btnSelectAll.setOnClickListener(v -> selectAllText());
        btnCopy.setOnClickListener(v -> copyText());
        btnCut.setOnClickListener(v -> cutText());
        btnPaste.setOnClickListener(v -> pasteText());
        
        // EditText 点击事件
        noteEditText.setOnClickListener(v -> {
            showKeyboard();
            hideTextMenu(); // 点击编辑框时隐藏菜单
        });
        
        // 点击外部区域隐藏菜单
        floatingView.setOnClickListener(v -> hideTextMenu());
    }

    private void setupTextSelection() {
        // 启用文本选择
        noteEditText.setTextIsSelectable(true);
        noteEditText.setLongClickable(true);
        
        // 长按显示文本菜单
        noteEditText.setOnLongClickListener(v -> {
            showTextMenu();
            return true;
        });
    }

    private void toggleTextMenu() {
        if (textMenuLayout.getVisibility() == View.VISIBLE) {
            hideTextMenu();
        } else {
            showTextMenu();
        }
    }

    private void showTextMenu() {
        textMenuLayout.setVisibility(View.VISIBLE);
        updateMenuButtonStates();
    }

    private void hideTextMenu() {
        textMenuLayout.setVisibility(View.GONE);
    }

    private void updateMenuButtonStates() {
        // 检查是否有文本被选中
        boolean hasSelection = noteEditText.hasSelection();
        int selectionStart = noteEditText.getSelectionStart();
        int selectionEnd = noteEditText.getSelectionEnd();
        boolean hasText = noteEditText.getText().length() > 0;
        
        // 更新按钮状态
        btnSelectAll.setEnabled(hasText);
        btnCopy.setEnabled(hasSelection);
        btnCut.setEnabled(hasSelection);
        
        // 检查剪贴板是否有内容
        boolean hasClipboard = clipboardManager.hasPrimaryClip();
        btnPaste.setEnabled(hasClipboard);
    }

    private void selectAllText() {
        noteEditText.selectAll();
        updateMenuButtonStates();
        Toast.makeText(this, "已全选", Toast.LENGTH_SHORT).show();
    }

    private void copyText() {
        int selectionStart = noteEditText.getSelectionStart();
        int selectionEnd = noteEditText.getSelectionEnd();
        
        if (selectionStart != selectionEnd) {
            String selectedText = noteEditText.getText().toString()
                    .substring(selectionStart, selectionEnd);
            
            ClipData clipData = ClipData.newPlainText("text", selectedText);
            clipboardManager.setPrimaryClip(clipData);
            
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
        }
        hideTextMenu();
    }

    private void cutText() {
        int selectionStart = noteEditText.getSelectionStart();
        int selectionEnd = noteEditText.getSelectionEnd();
        
        if (selectionStart != selectionEnd) {
            String selectedText = noteEditText.getText().toString()
                    .substring(selectionStart, selectionEnd);
            
            // 复制到剪贴板
            ClipData clipData = ClipData.newPlainText("text", selectedText);
            clipboardManager.setPrimaryClip(clipData);
            
            // 删除选中的文本
            noteEditText.getText().delete(selectionStart, selectionEnd);
            
            Toast.makeText(this, "已剪切", Toast.LENGTH_SHORT).show();
        }
        hideTextMenu();
    }

    private void pasteText() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence pasteText = clipData.getItemAt(0).getText();
                if (pasteText != null) {
                    int selectionStart = noteEditText.getSelectionStart();
                    int selectionEnd = noteEditText.getSelectionEnd();
                    
                    // 替换选中的文本或插入到光标位置
                    if (selectionStart != selectionEnd) {
                        noteEditText.getText().replace(selectionStart, selectionEnd, pasteText);
                    } else {
                        noteEditText.getText().insert(selectionStart, pasteText);
                    }
                    
                    Toast.makeText(this, "已粘贴", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "剪贴板为空", Toast.LENGTH_SHORT).show();
        }
        hideTextMenu();
    }

    private void showKeyboard() {
        noteEditText.requestFocus();
        inputMethodManager.showSoftInput(noteEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(noteEditText.getWindowToken(), 0);
    }

    private void setupDragFunctionality(View headerView) {
        final int[] initialCoords = new int[2];
        final float[] initialTouch = new float[2];
        final boolean[] isDragging = new boolean[1];

        headerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialCoords[0] = params.x;
                        initialCoords[1] = params.y;
                        initialTouch[0] = event.getRawX();
                        initialTouch[1] = event.getRawY();
                        isDragging[0] = false;
                        hideTextMenu();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialTouch[0];
                        float deltaY = event.getRawY() - initialTouch[1];
                        
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isDragging[0] = true;
                            hideKeyboard();
                            hideTextMenu();
                        }
                        
                        params.x = initialCoords[0] + (int) deltaX;
                        params.y = initialCoords[1] + (int) deltaY;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void minimizeNote() {
        hideKeyboard();
        hideTextMenu();
        
        if (noteEditText.getVisibility() == View.VISIBLE) {
            noteEditText.setVisibility(View.GONE);
            textMenuLayout.setVisibility(View.GONE);
        } else {
            noteEditText.setVisibility(View.VISIBLE);
        }
        windowManager.updateViewLayout(floatingView, params);
    }

    private void showSettingsPopup() {
        hideKeyboard();
        hideTextMenu();
        // 打开设置界面
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
