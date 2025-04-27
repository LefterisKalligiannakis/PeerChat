package com.example.peerchat.messenger;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.example.peerchat.MainActivity;
import com.example.peerchat.R;
import com.example.peerchat.logs.ChatDbHelper;
import com.example.peerchat.maps.MapsActivity;
import com.example.peerchat.p2p.ConnectionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;

// notifications
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class MessageActivity extends AppCompatActivity implements MessageListener {
    private ListView listViewMessages;
    private MessageAdapter messageAdapter;
    private EditText editTextMessage;
    private TextView connectionStatus;
    private ImageButton sendButton;
    private ImageButton mapButton;
    private MessageServer messageServer;
    private MessageClient messageClient;
    private boolean isHost;
    private ConnectionManager connectionManager;
    private boolean isInForeground;
    private ChatDbHelper dbHelper;

    FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude;
    private double currentLongitude;
    private double peerLatitude;
    private double peerLongitude;

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        createNotificationChannel();

        isInForeground = true;

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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        mapButton = findViewById(R.id.buttonMap);
        mapButton.setEnabled(false);
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("myLat", currentLatitude);
            intent.putExtra("myLng", currentLongitude);
            intent.putExtra("peerLat", peerLatitude);
            intent.putExtra("peerLng", peerLongitude);
            startActivity(intent);
        });

        isHost = getIntent().getBooleanExtra("isHost", false);
        if (isHost) {
            messageServer = new MessageServer(this);
            messageServer.start();
        } else {
            InetAddress hostAddress = getHostAddressFromIntent();
            if (hostAddress != null) {
                messageClient = new MessageClient(hostAddress, this);
                messageClient.start();
            } else {
                connectionStatus.setText("Invalid host address");
            }
        }
    }

    private InetAddress getHostAddressFromIntent() {
        String host = getIntent().getStringExtra("hostAddress");
        try {
            return InetAddress.getByName(host);
        } catch (Exception e) {
            Log.e("Chat", "Host address parsing error", e);
            return null;
        }
    }

    private void onMessageSent(String message) {
        messageAdapter.add("Me: " + message);
        scrollToBottom();

        new Thread(() -> {
            if (isHost && messageServer != null) {
                messageServer.write(message);
            } else if (messageClient != null) {
                messageClient.write(message);
            }
        }).start();
        sendLocationToPeer();
        dbHelper.insertMessage(System.currentTimeMillis(), message, true);
    }

    @Override
    public void onDataReceived(String message) {
        try {
            JSONObject json = new JSONObject(message);
            if (json.getString("type").equals("location")) {
                peerLatitude = json.getDouble("lat");
                peerLongitude = json.getDouble("lng");
                if(peerLatitude != 0 && peerLongitude != 0)
                    mapButton.setEnabled(true);

                Log.d("Chat", "Received peer location: " + peerLatitude + ", " + peerLongitude);
                return;
            }
        } catch (JSONException e) {
            // is a message
        }

        runOnUiThread(() -> {
            messageAdapter.add("Peer: " + message);
            dbHelper.insertMessage(System.currentTimeMillis(), message, false);
            scrollToBottom();

            if (!isInForeground) {
                showNotification("New Message", message);
            }
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void getLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Log.d("Location", "Lat: " + currentLatitude + ", Lng: " + currentLongitude);
                    }
                });
    }

    private void sendLocationToPeer() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", "location");
            json.put("lat", currentLatitude);
            json.put("lng", currentLongitude);
        } catch (JSONException e) {
            Log.e("Location", "Error creating location JSON", e);
        }
        String locationMessage = json.toString();

        new Thread(() -> {
            if (isHost && messageServer != null) {
                messageServer.write(locationMessage);
            } else if (messageClient != null) {
                messageClient.write(locationMessage);
            }
        }).start();
    }

    private boolean isConnecting = false;

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        if (isConnecting) {
            return;
        }

        if (!connected) {
            isConnecting = true; // Start connecting
            connectionStatus.setText("Connecting...");

            if (!isInForeground) {
                showNotification("Peer Disconnected", "Trying to reconnect...");
            }

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    isConnecting = false;
                    runOnUiThread(() -> connectionStatus.setText("Connected"));
                } catch (InterruptedException e) {
                    isConnecting = false;
                    runOnUiThread(() -> connectionStatus.setText("Disconnected"));
                }
            }).start();
        } else {
            runOnUiThread(() -> {
                connectionStatus.setText("Connected");
            });
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "Messages";
        String description = "Channel for message notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("MESSAGE_CHANNEL", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }


    private void scrollToBottom() {
        int count = messageAdapter.getCount();
        if (count > 0) {
            listViewMessages.setSelection(count - 1);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageServer != null) messageServer.close();
        if (messageClient != null) messageClient.close();
    }
}
