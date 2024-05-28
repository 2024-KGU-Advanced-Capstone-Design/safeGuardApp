package com.example.safeguardapp.data.model;

public class EmergencyCommentItem {
    private String topkey, content, commentator, commentdate;

    public EmergencyCommentItem(String topkey, String content, String commentator, String commentdate){
        this.topkey = topkey;
        this.content = content;
        this.commentator = commentator;
        this.commentdate = commentdate;
    }

    public String getTopkey() {return topkey;}
    public String getContent() {return content;}
    public String getCommentator() {return commentator;}
    public String getCommentdate() {return commentdate;}
}
