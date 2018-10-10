package gps.support.com;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback, LocationHelper.LocationCallBack {

    private GoogleMap mMap;
    private ProgressBar progress;
    private LocationHelper locationHelper;
    private FloatingActionButton btnMyLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = view.findViewById(R.id.progress);
        btnMyLocation = view.findViewById(R.id.btn_my_location);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationHelper = new LocationHelper(getActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        btnMyLocation.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        locationHelper.setTimeOut(60000).requestLocationGoogle(MapFragment.this);

        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                btnMyLocation.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                locationHelper.requestLocationGoogle(MapFragment.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("RestrictedApi")
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
