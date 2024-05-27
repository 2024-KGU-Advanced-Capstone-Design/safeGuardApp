package com.example.safeguardapp.data.model;

public class Notice {
    private String title;
    private String content;
    private String date;
    private String type;
    private String child;

    public Notice(String title, String content, String date, String type, String child){
        this.title = title;
        this.content = content;
        this.date = date;
        this.type = type;
        this.child = child;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }
}
