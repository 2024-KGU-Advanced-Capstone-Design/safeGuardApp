package com.example.safeguardapp.data.model;

import java.util.UUID;

public class ReceivedEmergencyItem {
    private String topkey, title, content, date, memberId, childName, alertText;
    private final String otherEmergencyUuid = UUID.randomUUID().toString();

    public ReceivedEmergencyItem(String topkey, String title, String content, String date, String childName, String memberId, String alertText) {
        this.topkey = topkey;
        this.title = title;
        this.content = content;
        this.date = date;
        this.memberId = memberId;
        this.childName = childName;
        this.alertText = alertText;
    }

    public String getTopkey(){return topkey;}
    public String getTitle(){return title;}
    public String getContent(){return content;}
    public String getDate(){return date;}
    public String getChildName() {return childName;}
    public String getMemberId(){return memberId;}
    public String getAlertText() {return alertText;}
    public String getOtherEmergencyUuid(){return otherEmergencyUuid;}
}
