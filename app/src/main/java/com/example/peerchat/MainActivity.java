package com.example.peerchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView connectionStatus, messages;
    Button discoverButton;
    ListView discoveredPeers;
    EditText messageInput;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;

    WIFIBroadcastReceiver receiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNames = new String[peers.size()];
    WifiP2pDevice[] devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        init();
        discoverListener();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void init() {
        connectionStatus = findViewById(R.id.connectionStatus);
        messages = findViewById(R.id.messages);
        discoverButton = findViewById(R.id.discoverButton);
        discoveredPeers = findViewById(R.id.discoveredPeers);
        messageInput = findViewById(R.id.messageInput);

        //initialize manager
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(),null);
        receiver = new WIFIBroadcastReceiver(channel,this , manager);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            //populate tbe listview with peers
            if(!wifiP2pDeviceList.equals(peers)){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                deviceNames = new String[wifiP2pDeviceList.getDeviceList().size()];
                devices = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    deviceNames[peers.indexOf(device)] = device.deviceName;
                    devices[index++] = device; // populate devices array
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);
                discoveredPeers.setAdapter(adapter);

                if (peers.isEmpty()) {
                    connectionStatus.setText("No Peers Found");
                    return;
                }

                discoveredPeers.setOnItemClickListener(new OnItemClickListener() {
                    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        // Ensure the devices array is properly populated before accessing
                        if (devices != null && devices.length > position) {
                            final WifiP2pDevice device = devices[position];  // Get the clicked device

                            // Create WifiP2pConfig to configure the connection
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;  // Set the device address of the clicked device

                            // Initiate connection request to the device
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                connectToDevice(config, device);
                            }
                        }
                    }
                });
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void connectToDevice(WifiP2pConfig config, WifiP2pDevice device) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES},
                            100);
                    return;
                }

                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Handle success
                        Log.d("WifiP2P", "Successfully connected to " + device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        // Handle failure
                        Log.d("WifiP2P", "Failed to connect to " + device.deviceName + " reason: " + reason);
                    }
                });
    }


    private void discoverListener() {
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if permissions are granted
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

                    // Request missing permissions
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES},
                            100);

                    return;
                }

                // Start discovering peers
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovering Peers, make sure to accept permission to see device name");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovering Failed: " + reason);
                    }
                });
            }
        });
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                discoverListener();
            } else {
                Toast.makeText(this, "Permissions Denied. Cannot discover peers.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);

    }
}