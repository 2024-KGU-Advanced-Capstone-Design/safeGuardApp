package com.example.safeguardapp.data.model;

public class ReceivedEmergencyItem {
    private String topkey, title, content, date, type, childName, alertText;

    public ReceivedEmergencyItem(String topkey, String title, String content, String date, String type, String childName, String alertText) {
        this.topkey = topkey;
        this.title = title;
        this.content = content;
        this.date = date;
        this.type = type;
        this.childName = childName;
        this.alertText = alertText;
    }

    public String getTopkey(){return topkey;}
    public String getTitle(){return title;}
    public String getContent(){return content;}
    public String getDate(){return date;}
    public String getType(){return type;}
    public String getChildName() {return childName;}
    public String getAlertText() {return alertText;}
}
