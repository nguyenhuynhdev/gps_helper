package gps.support.com;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationHelper.LocationCallBack {

    private GoogleMap mMap;
    private ProgressBar progress;
    private FloatingActionButton btnMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        progress = findViewById(R.id.progress);
        btnMyLocation = findViewById(R.id.btn_my_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationHelper.getInstance(this).onRequestPermissionsResult(requestCode, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LocationHelper.getInstance(this).onActivityResult(requestCode, resultCode, this);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String message) {
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        btnMyLocation.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        LocationHelper.getInstance(MapsActivity.this).setTimeOut(60000).requestLocation(
                MapsActivity.this, MapsActivity.this);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMyLocation.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                LocationHelper.getInstance(MapsActivity.this).requestLocation(
                        MapsActivity.this, MapsActivity.this);
            }
        });
    }

    @Override
    public void onResponse(LocationHelper.Task task) {
        btnMyLocation.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        mMap.clear();
        if (task.isSuccess()) {
            //LatLng needed
            LatLng latLng = new LatLng(task.getLocation().getLatitude(), task.getLocation()
                    .getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("My location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 30f));
        } else {
            showToast(task.getException().getMessage());
        }
    }

}
