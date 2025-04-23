package com.example.peerchat.messenger;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MessageClient extends Thread {
    private static final int PORT = 8888;
    private final java.net.InetAddress hostAddress;
    private final MessageListener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public MessageClient(java.net.InetAddress hostAddress, MessageListener listener) {
        this.hostAddress = hostAddress;
        this.listener = listener;
        this.socket = new Socket();
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress, PORT), 5000);
            Log.d("Chat", "Connected to server: " + hostAddress.getHostAddress());

            writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            uiHandler.post(() -> listener.onConnectionStatusChanged(true));

            String line;
            while ((line = reader.readLine()) != null) {
                final String msg = line;
                Log.d("Chat", "Received from server: " + msg);
                uiHandler.post(() -> listener.onDataReceived(msg));
            }
        } catch (IOException e) {
            Log.e("Chat", "Client error", e);
            uiHandler.post(() -> listener.onConnectionStatusChanged(false));
        } finally {
            close();
        }
    }

    public synchronized void write(String message) {
        try {
            if (writer != null) {
                writer.println(message);
            } else {
                Log.e("Chat", "Client writer is null");
            }
        } catch (Exception e) {
            Log.e("Chat", "Client write error", e);
        }
    }

    public void close() {
        try {
            Log.d("Chat", "Closing client...");
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            uiHandler.post(() -> listener.onConnectionStatusChanged(false));
        } catch (IOException e) {
            Log.e("Chat", "Client close error", e);
        }
    }
}