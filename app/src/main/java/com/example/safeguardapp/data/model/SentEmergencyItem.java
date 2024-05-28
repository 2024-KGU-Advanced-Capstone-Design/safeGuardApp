package com.example.safeguardapp.data.model;

import java.util.UUID;

public class SentEmergencyItem {
    private String topkey, title, content, date, memberId, childName, alertText;
    private final String myEmergencyUuid = UUID.randomUUID().toString();

    public SentEmergencyItem(String topkey, String title, String content, String date, String childName, String memberId, String alertText) {
        this.topkey = topkey;
        this.title = title;
        this.content = content;
        this.date = date;
        this.childName = childName;
        this.memberId = memberId;
        this.alertText = alertText;
    }

    public String getMyEmergencyUuid() {
        return myEmergencyUuid;
    }
    public String getTopkey(){return topkey;}
    public String getTitle(){return title;}
    public String getContent(){return content;}
    public String getDate(){return date;}
    public String getMemberId(){return memberId;}
    public String getChildName() {return childName;}
    public String getAlertText() {return alertText;}

}
