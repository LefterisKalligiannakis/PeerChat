package com.example.peerchat.p2p;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.*;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private static ConnectionManager instance;
    private final Context context;
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final WIFIBroadcastReceiver receiver;
    private final IntentFilter intentFilter;

    private ListView discoveredPeers;
    List<WifiP2pDevice> peers = new ArrayList<>();
    WifiP2pDevice[] devices;
    boolean isHost;

    // for moving to message activity
    public interface ConnectionCallback {
        void onConnectionEstablished(boolean isHost, InetAddress hostAddress);
    }

    // Private constructor
    private ConnectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.receiver = new WIFIBroadcastReceiver(channel, this, manager);

        this.intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public static synchronized ConnectionManager getInstance(Context context, ListView discoveredPeers) {
        if (instance == null) {
            instance = new ConnectionManager(context);
        }
        instance.discoveredPeers = discoveredPeers;
        return instance;
    }

    public void registerReceiver() {
        context.registerReceiver(receiver, intentFilter);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    public void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissions not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("P2P", "Discovery Started");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("P2P", "Discovery Failed: " + reason);
            }
        });
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            devices = new WifiP2pDevice[peers.size()];
            String[] deviceNames = new String[peers.size()];

            int index = 0;
            for (WifiP2pDevice device : peers) {
                deviceNames[index] = device.deviceName;
                devices[index++] = device;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, deviceNames);
            discoveredPeers.setAdapter(adapter);

            discoveredPeers.setOnItemClickListener((parent, view, position, id) -> {
                if (devices != null && position < devices.length) {
                    connectToDevice(devices[position]);
                }
            });
        }
    };

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("P2P", "Connected to: " + device.deviceName);
            }

            @Override
            public void onFailure(int reason) {
                Log.e("P2P", "Connection failed: " + reason);
            }
        });
    }

    public void disconnectFromDevice() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("P2P", "Disconnected");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("P2P", "Disconnection failed: " + reason);
            }
        });
    }


    private ConnectionCallback connectionCallback;
    public void setConnectionCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    public final WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                isHost = true;
            } else if (info.groupFormed) {
                isHost = false;
            }

            // Start the MessageActivity
            if (connectionCallback != null) {
                connectionCallback.onConnectionEstablished(isHost, groupOwnerAddress);
            }
        }
    };


}
