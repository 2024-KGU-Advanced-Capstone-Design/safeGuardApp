package com.example.safeguardapp.Notice;

public class GetNotificationResponse {
    private String title;
    private String content;
    private String date;
    private String type;
    private String child;

    public GetNotificationResponse(String title, String content, String date, String type, String child){
        this.title = title;
        this.content = content;
        this.date = date;
        this.type = type;
        this.child = child;
    }

    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getChild() {
        return child;
    }
}
