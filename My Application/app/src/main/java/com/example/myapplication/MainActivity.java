package com.example.myapplication; // 替换为你的实际包名

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Entry> allEntries = new ArrayList<>(); // 所有词条
    private List<Entry> filteredEntries = new ArrayList<>(); // 搜索过滤后的词条
    private EntryAdapter adapter;
    private EditText etSearch;
    private int editPosition = -1; // 编辑模式的位置（-1为添加模式）

    // 权限请求码
    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 权限适配：覆盖Android 6.0-14全版本
        requestLegacyStoragePermission();
        checkAndroid13PlusPermissions();

        // 初始化控件
        etSearch = findViewById(R.id.et_search);
        ListView lvEntries = findViewById(R.id.lv_entries);
        Button btnAdd = findViewById(R.id.btn_add);

        // 初始化适配器
        adapter = new EntryAdapter();
        lvEntries.setAdapter(adapter);

        // 搜索功能（实时过滤标题/内容）
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEntries(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 添加词条
        btnAdd.setOnClickListener(v -> showEditDialog(null, -1));

        // 列表item长按：编辑/删除/导出/导入
        lvEntries.setOnItemLongClickListener((parent, view, position, id) -> {
            Entry entry = filteredEntries.get(position);
            showItemMenu(entry, position);
            return true;
        });

        // 列表item点击：查看详情
        lvEntries.setOnItemClickListener((parent, view, position, id) -> {
            Entry entry = filteredEntries.get(position);
            showDetailDialog(entry);
        });
    }

    // 过滤词条（关键词匹配标题/内容）
    private void filterEntries(String keyword) {
        filteredEntries.clear();
        if (keyword.isEmpty()) {
            filteredEntries.addAll(allEntries);
        } else {
            for (Entry entry : allEntries) {
                if (entry.getTitle().toLowerCase().contains(keyword.toLowerCase()) 
                    || entry.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredEntries.add(entry);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // 词条操作菜单
    private void showItemMenu(Entry entry, int position) {
        new AlertDialog.Builder(this)
                .setTitle("操作")
                .setItems(new String[]{"编辑", "删除", "导出所有词条", "导入词条"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // 编辑
                            editPosition = allEntries.indexOf(entry);
                            showEditDialog(entry, editPosition);
                            break;
                        case 1: // 删除
                            deleteEntry(position);
                            break;
                        case 2: // 导出
                            exportEntries();
                            break;
                        case 3: // 导入
                            importEntries();
                            break;
                    }
                })
                .show();
    }

    // 添加/编辑弹窗
    private void showEditDialog(Entry entry, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // 编辑模式填充内容
        if (entry != null) {
            etTitle.setText(entry.getTitle());
            etContent.setText(entry.getContent());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(entry == null ? "添加词条" : "编辑词条")
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(MainActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (position == -1) { // 添加
                allEntries.add(new Entry(title, content));
            } else { // 编辑
                allEntries.get(position).setTitle(title);
                allEntries.get(position).setContent(content);
            }

            filterEntries(etSearch.getText().toString());
            dialog.dismiss();
            Toast.makeText(MainActivity.this, entry == null ? "添加成功" : "修改成功", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // 查看详情
    private void showDetailDialog(Entry entry) {
        new AlertDialog.Builder(this)
                .setTitle(entry.getTitle())
                .setMessage(entry.getContent())
                .setPositiveButton("确定", null)
                .show();
    }

    // 删除词条
    private void deleteEntry(int position) {
        Entry entry = filteredEntries.get(position);
        allEntries.remove(entry);
        filteredEntries.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
    }

    // 导出词条（TXT格式，存于手机根目录/词条管理）
    private void exportEntries() {
        if (allEntries.isEmpty()) {
            Toast.makeText(this, "暂无词条可导出", Toast.LENGTH_SHORT).show();
            return;
        }
        // 检查权限（二次确认）
        if (checkStoragePermission()) {
            boolean success = FileUtils.exportEntries(allEntries);
            Toast.makeText(this, success ? "导出成功（路径：手机根目录/词条管理）" : "导出失败", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "请先授予存储权限", Toast.LENGTH_SHORT).show();
            requestLegacyStoragePermission();
            checkAndroid13PlusPermissions();
        }
    }

    // 导入词条（读取根目录/词条管理/entries.txt）
    private void importEntries() {
        if (checkStoragePermission()) {
            List<Entry> imported = FileUtils.importEntries();
            if (imported.isEmpty()) {
                Toast.makeText(this, "未找到导入文件或文件为空", Toast.LENGTH_SHORT).show();
                return;
            }
            allEntries.addAll(imported);
            filterEntries(etSearch.getText().toString());
            Toast.makeText(this, "导入成功，共" + imported.size() + "条词条", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请先授予存储权限", Toast.LENGTH_SHORT).show();
            requestLegacyStoragePermission();
            checkAndroid13PlusPermissions();
        }
    }

    // Android 6.0-12 存储权限申请
    private void requestLegacyStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST);
            }
        }
    }

    // Android 13+ 媒体权限申请
    private void checkAndroid13PlusPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                }, PERMISSION_REQUEST);
            }
        }
    }

    // 检查存储权限是否已授予
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (!granted) {
                Toast.makeText(this, "未授予存储权限，导入导出功能无法使用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 列表适配器
    private class EntryAdapter extends BaseAdapter {
        @Override
        public int getCount() { return filteredEntries.size(); }

        @Override
        public Object getItem(int position) { return filteredEntries.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.tvTitle = convertView.findViewById(android.R.id.text1);
                holder.tvContent = convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Entry entry = filteredEntries.get(position);
            holder.tvTitle.setText(entry.getTitle());
            // 内容预览（截取前20字）
            String preview = entry.getContent().length() > 20 ? entry.getContent().substring(0, 20) + "..." : entry.getContent();
            holder.tvContent.setText(preview);

            return convertView;
        }

        class ViewHolder {
            TextView tvTitle;
            TextView tvContent;
        }
    }
}
