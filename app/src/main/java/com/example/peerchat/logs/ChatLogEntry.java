package com.example.peerchat.logs;

public class ChatLogEntry {
    private String message;
    private boolean isMe;
    private String timestamp;

    public ChatLogEntry(String message, boolean isMe, String timestamp) {
        this.message = message;
        this.isMe = isMe;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMe() {
        return isMe;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
