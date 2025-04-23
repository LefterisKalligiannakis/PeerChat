package com.example.peerchat.maps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.peerchat.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> {
            finish();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        Intent intent = getIntent();

        double myLat = intent.getDoubleExtra("myLat", 0);
        double myLng = intent.getDoubleExtra("myLng", 0);
        double peerLat = intent.getDoubleExtra("peerLat", 0);
        double peerLng = intent.getDoubleExtra("peerLng", 0);

        LatLng myLocation = new LatLng(myLat, myLng);
        LatLng peerLocation = new LatLng(peerLat, peerLng);

        map.addMarker(new MarkerOptions().position(myLocation).title("You"));
        map.addMarker(new MarkerOptions().position(peerLocation).title("Peer"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocation);
        builder.include(peerLocation);
        LatLngBounds bounds = builder.build();

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
}
