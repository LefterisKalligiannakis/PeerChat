package com.example.peerchat.p2p;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.annotation.RequiresPermission;
import android.annotation.SuppressLint;

public class WIFIBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ConnectionManager connectionManager;

    public WIFIBroadcastReceiver(WifiP2pManager.Channel channel, ConnectionManager connectionManager, WifiP2pManager manager) {
        this.channel = channel;
        this.connectionManager = connectionManager;
        this.manager = manager;
    }

    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                manager.requestPeers(channel, connectionManager.peerListListener);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) return;

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                manager.requestConnectionInfo(channel, connectionManager.connectionInfoListener);
            }
        }
    }
}
