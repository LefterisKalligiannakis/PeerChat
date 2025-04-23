package com.example.peerchat.messenger;

public interface MessageListener {
    void onDataReceived(String message);
    void onConnectionStatusChanged(boolean connected); }