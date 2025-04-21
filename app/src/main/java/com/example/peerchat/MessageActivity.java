package com.example.peerchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity implements MessageListener {
    private ListView listViewMessages;
    private MessageAdapter messageAdapter;
    private EditText editTextMessage;
    private TextView connectionStatus;
    private ImageButton sendButton;
    private MessageServer messageServer;
    private MessageClient messageClient;
    private boolean isHost;
    private ConnectionManager connectionManager;

    private ChatDbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        connectionStatus = findViewById(R.id.connectionStatus);
        listViewMessages = findViewById(R.id.listViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        ImageButton backButton = findViewById(R.id.buttonBack);
        connectionManager = ConnectionManager.getInstance(this, listViewMessages);
        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        listViewMessages.setAdapter(messageAdapter);

        dbHelper = new ChatDbHelper(this);

        sendButton.setEnabled(true);

        sendButton.setOnClickListener(v -> {
            String msg = editTextMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                onMessageSent(msg);
                editTextMessage.setText("");
            }
        });

        backButton.setOnClickListener(v -> {
            connectionManager.disconnectFromDevice();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear back stack
            startActivity(intent);
        });

        isHost = getIntent().getBooleanExtra("isHost", false);
        if (isHost) {
            connectionStatus.setText("Waiting for client...");
            messageServer = new MessageServer(this);
            messageServer.start();
        } else {
            connectionStatus.setText("Connecting to host...");
            java.net.InetAddress hostAddress = getHostAddressFromIntent();
            if (hostAddress != null) {
                messageClient = new MessageClient(hostAddress, this);
                messageClient.start();
            } else {
                connectionStatus.setText("Invalid host address");
            }
        }
    }

    private java.net.InetAddress getHostAddressFromIntent() {
        String host = getIntent().getStringExtra("hostAddress");
        try {
            return java.net.InetAddress.getByName(host);
        } catch (Exception e) {
            Log.e("Chat", "Host address parsing error", e);
            return null;
        }
    }

    private void onMessageSent(String message) {
        if (messageAdapter == null) {
            Log.e("Chat", "MessageAdapter is null");
            return;
        }
        messageAdapter.add("Me: " + message);
        scrollToBottom();

        if (isHost) {
            if (messageServer != null) {
                Log.d("Chat", "Host sending message off main thread: " + message);
                new Thread(() -> messageServer.write(message)).start();
            } else {
                Log.e("Chat", "messageServer is null");
            }
        } else {
            if (messageClient != null) {
                Log.d("Chat", "Client sending message off main thread: " + message);
                new Thread(() -> messageClient.write(message)).start();
            } else {
                Log.e("Chat", "messageClient is null");
            }
        }
        dbHelper.insertMessage(System.currentTimeMillis(), message, true);
    }

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> {
            messageAdapter.add("Peer: " + message);
            dbHelper.insertMessage(System.currentTimeMillis(), message, false);
            scrollToBottom();
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        runOnUiThread(() -> {
            connectionStatus.setText(connected ? "Connected" : (isHost ? "Waiting for client..." : "Connecting to host..."));
            sendButton.setEnabled(connected);
        });
    }

    private void scrollToBottom() {
        int count = messageAdapter.getCount();
        if (count > 0) {
            listViewMessages.setSelection(count - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageServer != null) messageServer.close();
        if (messageClient != null) messageClient.close();
    }
}