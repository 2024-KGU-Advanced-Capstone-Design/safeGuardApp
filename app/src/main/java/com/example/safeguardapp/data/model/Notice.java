package com.example.safeguardapp.data.model;

public class Notice {
    private String title;
    private String content;
    private String date;
    private String senderId;
    private String child;

    public Notice(String title, String content, String date, String child, String senderId){
        this.title = title;
        this.content = content;
        this.date = date;
        this.child = child;
        this.senderId = senderId;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }
}
