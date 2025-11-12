package com.example.entrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Entries.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TABLE_ENTRIES = "entries";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_UPDATE_TIME = "update_time";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_ENTRIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT NOT NULL,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_CREATE_TIME + " INTEGER,"
                + COLUMN_UPDATE_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        onCreate(db);
    }
    
    // 添加词条
    public long addEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, entry.getTitle());
        values.put(COLUMN_CONTENT, entry.getContent());
        values.put(COLUMN_CREATE_TIME, entry.getCreateTime());
        values.put(COLUMN_UPDATE_TIME, entry.getUpdateTime());
        
        long id = db.insert(TABLE_ENTRIES, null, values);
        db.close();
        return id;
    }
    
    // 获取所有词条
    public List<Entry> getAllEntries() {
        List<Entry> entries = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_ENTRIES + " ORDER BY " + COLUMN_UPDATE_TIME + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                Entry entry = new Entry();
                entry.setId(cursor.getInt(0));
                entry.setTitle(cursor.getString(1));
                entry.setContent(cursor.getString(2));
                entry.setCreateTime(cursor.getLong(3));
                entry.setUpdateTime(cursor.getLong(4));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }
    
    // 搜索词条
    public List<Entry> searchEntries(String keyword) {
        List<Entry> entries = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_ENTRIES + 
                " WHERE " + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?" +
                " ORDER BY " + COLUMN_UPDATE_TIME + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%", "%" + keyword + "%"});
        
        if (cursor.moveToFirst()) {
            do {
                Entry entry = new Entry();
                entry.setId(cursor.getInt(0));
                entry.setTitle(cursor.getString(1));
                entry.setContent(cursor.getString(2));
                entry.setCreateTime(cursor.getLong(3));
                entry.setUpdateTime(cursor.getLong(4));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }
    
    // 更新词条
    public int updateEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, entry.getTitle());
        values.put(COLUMN_CONTENT, entry.getContent());
        values.put(COLUMN_UPDATE_TIME, entry.getUpdateTime());
        
        return db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?", 
                new String[]{String.valueOf(entry.getId())});
    }
    
    // 删除词条
    public void deleteEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    
    // 获取词条数量
    public int getEntryCount() {
        String query = "SELECT COUNT(*) FROM " + TABLE_ENTRIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
