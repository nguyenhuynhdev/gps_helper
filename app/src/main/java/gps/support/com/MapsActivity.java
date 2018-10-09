package gps.support.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressBar progress;
    private FloatingActionButton btnMyLocation;
    private static final int LOCATION_PERMISSION = 2;

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
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == LOCATION_PERMISSION) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation();
                showToast("Permission granted");
            } else {
                showToast("User cancel, permission denied");
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String message) {
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getCurrentLocation();
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCurrentLocation();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void getCurrentLocation() {
        btnMyLocation.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] LOCATION_PERMS = {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(MapsActivity.this, LOCATION_PERMS, LOCATION_PERMISSION);
        } else {
            GpsHelper.getInstance(MapsActivity.this).getCurrentLocation(99999999, new GpsHelper.LocationCallBack() {
                @Override
                public void onResponse(GpsHelper.Task task) {
                    btnMyLocation.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);

                    if (task.isSuccess()) {
                        //LatLng needed
                        LatLng latLng = new LatLng(task.getLocation().getLatitude(), task.getLocation()
                                .getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 30f));
                    } else {
                        if (task.getException() instanceof ResolvableApiException) {
                            showToast("Please grant permission to continue");
                            try {
                                ((ResolvableApiException) task.getException())
                                        .startResolutionForResult(MapsActivity.this, LOCATION_PERMISSION);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showToast(task.getException().getLocalizedMessage());
                        }
                    }
                }
            });
        }
    }
}
