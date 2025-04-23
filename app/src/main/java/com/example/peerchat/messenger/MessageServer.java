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
import java.net.ServerSocket;
import java.net.Socket;

public class MessageServer extends Thread {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final MessageListener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public MessageServer(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            uiHandler.post(() -> listener.onConnectionStatusChanged(false));

            clientSocket = serverSocket.accept();

            writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream())), true);
            reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            uiHandler.post(() -> listener.onConnectionStatusChanged(true));

            String line;
            while ((line = reader.readLine()) != null) {
                final String msg = line;
                uiHandler.post(() -> listener.onDataReceived(msg));
            }
        } catch (IOException e) {
            Log.e("Chat", "Server error", e);
            uiHandler.post(() -> listener.onConnectionStatusChanged(false));
        } finally {
            close();
        }
    }

    public synchronized void write(String message) {
        try {
            if (writer != null) {
                Log.d("Chat", "Server sending: " + message);
                writer.println(message);
            } else {
                Log.e("Chat", "Server writer is null");
            }
        } catch (Exception e) {
            Log.e("Chat", "Server write error", e);
        }
    }

    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            uiHandler.post(() -> listener.onConnectionStatusChanged(false));
        } catch (IOException e) {
            Log.e("Chat", "Server close error", e);
        }
    }
}