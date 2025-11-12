package com.example.myapplication; // 替换为你的实际包名

public class Entry {
    private String title; // 词条标题（搜索关键词）
    private String content; // 词条内容

    public Entry(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // getter/setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
