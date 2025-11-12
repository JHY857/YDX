package com.example.entrymanager;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String EXPORT_PATH = 
        Environment.getExternalStorageDirectory() + "/EntryManager/";
    private static final String FILE_NAME = "entries.txt";
    
    public static boolean exportEntries(List<Entry> entries) {
        try {
            // 确保目录存在
            File dir = new File(EXPORT_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, FILE_NAME);
            FileWriter writer = new FileWriter(file);
            
            for (Entry entry : entries) {
                // 使用特殊分隔符保存数据
                String line = entry.getTitle() + "|||" + entry.getContent() + "\n";
                writer.write(line);
            }
            
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<Entry> importEntries(List<Entry> currentEntries) {
        List<Entry> importedEntries = new ArrayList<>();
        
        try {
            File file = new File(EXPORT_PATH, FILE_NAME);
            if (!file.exists()) {
                return importedEntries;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("|||")) {
                    String[] parts = line.split("\\|\\|\\|", 2);
                    if (parts.length == 2) {
                        String title = parts[0].trim();
                        String content = parts[1].trim();
                        
                        // 检查是否已存在相同标题的词条
                        boolean exists = false;
                        for (Entry existing : currentEntries) {
                            if (existing.getTitle().equals(title)) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists) {
                            Entry newEntry = new Entry(title, content);
                            importedEntries.add(newEntry);
                        }
                    }
                }
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return importedEntries;
    }
}
