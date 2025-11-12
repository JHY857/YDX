package com.example.myapplication; // 替换为你的实际包名

import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    // 导出路径：手机根目录/词条管理
    private static final String EXPORT_PATH = Environment.getExternalStorageDirectory() + "/词条管理/";
    private static final String FILE_NAME = "entries.txt";

    // 导出词条到TXT
    public static boolean exportEntries(List<Entry> entries) {
        File dir = new File(EXPORT_PATH);
        if (!dir.exists()) dir.mkdirs(); // 自动创建目录

        File file = new File(dir, FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (Entry entry : entries) {
                // 格式：标题|内容（避免内容含|时分割错误）
                String line = entry.getTitle() + "|" + entry.getContent() + "\n";
                fos.write(line.getBytes("UTF-8"));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 从TXT导入词条
    public static List<Entry> importEntries() {
        List<Entry> entries = new ArrayList<>();
        File file = new File(EXPORT_PATH, FILE_NAME);
        if (!file.exists()) return entries;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            String content = new String(buffer, "UTF-8");
            String[] lines = content.split("\n");

            for (String line : lines) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 2); // 仅分割一次
                    if (parts.length == 2) {
                        entries.add(new Entry(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
