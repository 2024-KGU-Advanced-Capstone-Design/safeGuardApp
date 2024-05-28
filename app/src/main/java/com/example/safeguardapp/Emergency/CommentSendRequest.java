package com.example.safeguardapp.Emergency;

public class CommentSendRequest {
    String commentatorId, commentContent, emergencyId;
    public CommentSendRequest(String commentatorId, String commentContent, String emergencyId){
        this.commentatorId = commentatorId;
        this.commentContent = commentContent;
        this.emergencyId = emergencyId;
    }
}
