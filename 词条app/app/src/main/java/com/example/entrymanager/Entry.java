package com.example.entrymanager;

public class Entry {
    private int id;
    private String title;
    private String content;
    private long createTime;
    private long updateTime;

    public Entry() {}

    public Entry(String title, String content) {
        this.title = title;
        this.content = content;
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public Entry(int id, String title, String content, long createTime, long updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        this.updateTime = System.currentTimeMillis();
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        this.updateTime = System.currentTimeMillis();
    }
    
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    
    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}
