package com.example.peerchat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;


public class WIFIBroadcastReceiver extends BroadcastReceiver {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    //Constructor
    public WIFIBroadcastReceiver(WifiP2pManager.Channel channel, MainActivity activity, WifiP2pManager manager) {
        this.channel = channel;
        this.activity = activity;
        this.manager = manager;
    }

    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Request peers after confirming permissions are granted
                manager.requestPeers(channel, activity.peerListListener);

        }

        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // check if connected
            activity.connectionStatus.setText("Connected");
        }

    }
}
