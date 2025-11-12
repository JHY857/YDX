package com.example.entrymanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private List<Entry> allEntries = new ArrayList<>();
    private List<Entry> displayedEntries = new ArrayList<>();
    private EntryAdapter adapter;
    private DatabaseHelper databaseHelper;
    
    private EditText etSearch;
    private ListView lvEntries;
    private TextView tvStats;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initDatabase();
        initViews();
        loadEntries();
    }
    
    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }
    
    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        lvEntries = findViewById(R.id.lv_entries);
        tvStats = findViewById(R.id.tv_stats);
        Button btnAdd = findViewById(R.id.btn_add);
        
        adapter = new EntryAdapter();
        lvEntries.setAdapter(adapter);
        
        // 搜索功能
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEntries(s.toString());
            }
        });
        
        btnAdd.setOnClickListener(v -> showEditDialog(null));
        
        lvEntries.setOnItemClickListener((parent, view, position, id) -> {
            Entry entry = displayedEntries.get(position);
            showDetailDialog(entry);
        });
        
        lvEntries.setOnItemLongClickListener((parent, view, position, id) -> {
            Entry entry = displayedEntries.get(position);
            showOperationDialog(entry, position);
            return true;
        });
    }
    
    private void loadEntries() {
        allEntries = databaseHelper.getAllEntries();
        displayedEntries.clear();
        displayedEntries.addAll(allEntries);
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    private void filterEntries(String keyword) {
        displayedEntries.clear();
        if (keyword.isEmpty()) {
            displayedEntries.addAll(allEntries);
        } else {
            List<Entry> results = new ArrayList<>();
            for (Entry entry : allEntries) {
                if (entry.getTitle().toLowerCase().contains(keyword.toLowerCase()) || 
                    entry.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                    results.add(entry);
                }
            }
            displayedEntries.addAll(results);
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    private void updateStats() {
        tvStats.setText("共 " + displayedEntries.size() + " 个词条");
    }
    
    private void showEditDialog(Entry entry) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        
        if (entry != null) {
            etTitle.setText(entry.getTitle());
            etContent.setText(entry.getContent());
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(entry == null ? "添加词条" : "编辑词条")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null)
            .create();
            
        dialog.show();
        
        // 自定义确定按钮点击事件
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (entry == null) {
                // 添加
                Entry newEntry = new Entry(title, content);
                long id = databaseHelper.addEntry(newEntry);
                if (id != -1) {
                    newEntry.setId((int) id);
                    allEntries.add(0, newEntry);
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 编辑
                entry.setTitle(title);
                entry.setContent(content);
                databaseHelper.updateEntry(entry);
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
            }
            
            filterEntries(etSearch.getText().toString());
            dialog.dismiss();
        });
    }
    
    private void showDetailDialog(Entry entry) {
        new AlertDialog.Builder(this)
            .setTitle(entry.getTitle())
            .setMessage(entry.getContent())
            .setPositiveButton("确定", null)
            .show();
    }
    
    private void showOperationDialog(Entry entry, int position) {
        String[] options = {"查看", "编辑", "删除", "导出", "导入"};
        
        new AlertDialog.Builder(this)
            .setTitle("操作")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: showDetailDialog(entry); break;
                    case 1: showEditDialog(entry); break;
                    case 2: deleteEntry(entry, position); break;
                    case 3: exportEntries(); break;
                    case 4: importEntries(); break;
                }
            })
            .show();
    }
    
    private void deleteEntry(Entry entry, int position) {
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("删除《" + entry.getTitle() + "》？")
            .setPositiveButton("删除", (d, w) -> {
                databaseHelper.deleteEntry(entry.getId());
                allEntries.remove(entry);
                displayedEntries.remove(position);
                adapter.notifyDataSetChanged();
                updateStats();
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void exportEntries() {
        boolean success = FileUtils.exportEntries(allEntries);
        Toast.makeText(this, success ? "导出成功" : "导出失败", Toast.LENGTH_SHORT).show();
    }
    
    private void importEntries() {
        List<Entry> imported = FileUtils.importEntries(allEntries);
        if (imported.isEmpty()) {
            Toast.makeText(this, "没有可导入的词条", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int count = 0;
        for (Entry entry : imported) {
            long id = databaseHelper.addEntry(entry);
            if (id != -1) {
                entry.setId((int) id);
                allEntries.add(0, entry);
                count++;
            }
        }
        
        filterEntries(etSearch.getText().toString());
        Toast.makeText(this, "导入 " + count + " 个词条", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
    
    // 适配器
    private class EntryAdapter extends BaseAdapter {
        public int getCount() { return displayedEntries.size(); }
        public Object getItem(int position) { return displayedEntries.get(position); }
        public long getItemId(int position) { return position; }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.text1 = convertView.findViewById(android.R.id.text1);
                holder.text2 = convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Entry entry = displayedEntries.get(position);
            holder.text1.setText(entry.getTitle());
            
            String content = entry.getContent();
            if (content.length() > 30) content = content.substring(0, 30) + "...";
            holder.text2.setText(content);
            
            return convertView;
        }
        
        class ViewHolder {
            TextView text1, text2;
        }
    }
}
