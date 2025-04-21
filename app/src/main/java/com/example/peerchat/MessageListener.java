package com.example.peerchat;

public interface MessageListener {
    void onMessageReceived(String message);
    void onConnectionStatusChanged(boolean connected); }