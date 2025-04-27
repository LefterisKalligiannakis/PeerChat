package com.example.peerchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.peerchat.logs.ChatLogActivity;
import com.example.peerchat.messenger.MessageActivity;
import com.example.peerchat.p2p.ConnectionManager;


public class MainActivity extends AppCompatActivity {

    private ConnectionManager connectionManager;
    private ListView discoveredPeers;
    private Button discoverButton;
    private TextView connectionStatus;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        discoveredPeers = findViewById(R.id.discoveredPeers);
        connectionStatus = findViewById(R.id.connectionStatus);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }


        // initialize connection manager and callback
        connectionManager = ConnectionManager.getInstance(this, discoveredPeers);
        connectionManager.setConnectionCallback((isHost, hostAddress) -> {
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            intent.putExtra("isHost", isHost);
            intent.putExtra("hostAddress", hostAddress.getHostAddress());
            startActivity(intent);
        });

        discoverButton = findViewById(R.id.discoverButton);

        discoverButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                connectionManager.discoverPeers();
                connectionStatus.setText("Discovering peers...");
                discoverButton.setEnabled(false);
                discoverButton.setBackgroundColor(Color.parseColor("#5487b0")); // darker shade
            }
        });

        findViewById(R.id.viewLogsButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatLogActivity.class));
        });
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES},
                    100);
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        discoverButton.setBackgroundColor(Color.parseColor("#90CAF9"));
        discoverButton.setEnabled(true);
        connectionManager.registerReceiver();

    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionManager.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // on destroy disconnect from device
        connectionManager.disconnectFromDevice();
    }


}
