package gps.support.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Serializable;

/**
 * Helper class to get current location
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LocationHelper extends LocationCallback implements LocationListener, Runnable {

    private static final int LOCATION_PERMISSION = 4321;
    private static final int LOCATION_PERMISSION_GOOGLE = 1234;

    private Activity activity;
    //Handler to request timeout
    private final Handler handler;
    private LocationCallBack mCallBack;
    //Default max
    private long timeOut = Long.MAX_VALUE;
    private final LocationManager locationManager;
    private final FusedLocationProviderClient locationProviderClient;


    public LocationHelper(Activity activity) {
        this.activity = activity;
        handler = new Handler(Looper.getMainLooper());
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Init in [{@link Activity}.onRequestPermissionsResult]
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation(mCallBack);
                    return;
                }
            }
            case LOCATION_PERMISSION_GOOGLE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationGoogle(mCallBack);
                    return;
                }
            }
        }
        mCallBack.onResponse(new Task(null,
                new Throwable("Permission denied"), false));
    }

    /**
     * Init in [{@link Activity}.onActivityResult]
     */
    public void onActivityResult(int requestCode, int resultCode) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (resultCode == Activity.RESULT_OK) {
                    requestLocation(mCallBack);
                    return;
                }
            }
            case LOCATION_PERMISSION_GOOGLE: {
                if (resultCode == Activity.RESULT_OK) {
                    requestLocationGoogle(mCallBack);
                    return;
                }
            }
        }
        mCallBack.onResponse(new Task(null,
                new Throwable("Permission denied"), false));

    }

    /**
     * Get current location using [{@link FusedLocationProviderClient}]
     *
     * @param locationCallBack class implement[{@link LocationCallBack}]
     */
    public void requestLocationGoogle(LocationCallBack locationCallBack) {
        try {
            mCallBack = locationCallBack;
            handler.removeCallbacks(this);
            handler.postDelayed(this, timeOut);
            //check gps everyTime call this function
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1);
            locationRequest.setFastestInterval(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(activity);
            com.google.android.gms.tasks.Task<LocationSettingsResponse> task =
                    client.checkLocationSettings(builder.build());
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        handler.removeCallbacks(LocationHelper.this);
                        try {
                            //Show dialog to select LocationRequest.PRIORITY_HIGH_ACCURACY
                            ((ResolvableApiException) e)
                                    .startResolutionForResult(activity, LOCATION_PERMISSION_GOOGLE);
                        } catch (IntentSender.SendIntentException e1) {
                            mCallBack.onResponse(new Task(null, e1, false));
                        }
                    }
                }
            });
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        String[] LOCATION_PERMS = {
                                Manifest.permission.ACCESS_FINE_LOCATION
                        };
                        ActivityCompat.requestPermissions(activity,
                                LOCATION_PERMS, LOCATION_PERMISSION_GOOGLE);
                        return;
                    }
                    locationProviderClient.requestLocationUpdates(locationRequest,
                            LocationHelper.this, null);
                }
            });
        } finally {
            locationProviderClient.removeLocationUpdates(this);
        }
    }

    /**
     * Get current location using [{@link LocationManager}]
     *
     * @param locationCallBack class implement[{@link LocationCallBack}]
     */
    public void requestLocation(LocationCallBack locationCallBack) {
        try {
            mCallBack = locationCallBack;
            handler.removeCallbacks(this);
            handler.postDelayed(this, timeOut);
            //check gps everyTime call this function
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(activity);
            com.google.android.gms.tasks.Task<LocationSettingsResponse> task =
                    client.checkLocationSettings(builder.build());
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        handler.removeCallbacks(LocationHelper.this);
                        try {
                            //Show dialog to select LocationRequest.PRIORITY_HIGH_ACCURACY
                            ((ResolvableApiException) e)
                                    .startResolutionForResult(activity, LOCATION_PERMISSION);
                        } catch (IntentSender.SendIntentException e1) {
                            mCallBack.onResponse(new Task(null, e1, false));
                        }
                    }
                }
            });
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        String[] LOCATION_PERMS = {
                                Manifest.permission.ACCESS_FINE_LOCATION
                        };
                        ActivityCompat.requestPermissions(activity,
                                LOCATION_PERMS, LOCATION_PERMISSION);
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1, 0, LocationHelper.this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1, 0, LocationHelper.this);
                }
            });
        } finally {
            locationManager.removeUpdates(this);
        }
    }

    /**
     * Set time out of request
     * @param timeOut millis
     * @return current LocationHelper
     */
    public LocationHelper setTimeOut(long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    /**
     * extends [{@link LocationCallback}]
     *
     * @param locationResult Locations result
     */
    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult != null && locationResult.getLastLocation() != null) {
            handler.removeCallbacks(this);
            locationProviderClient.removeLocationUpdates(this);
            mCallBack.onResponse(new Task(locationResult.getLastLocation(), null, true));
        }
    }

    /**
     * extends [{@link LocationCallback}]
     *
     * @param locationAvailability check isLocation Available
     */
    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        if (!locationAvailability.isLocationAvailable()) {
            handler.removeCallbacks(this);
            locationProviderClient.removeLocationUpdates(this);
            mCallBack.onResponse(new Task(null, new Throwable("Location is not available"), false));
        }
    }

    /**
     * implement [{@link LocationListener}]
     *
     * @param location Location result
     */
    @Override
    public void onLocationChanged(Location location) {
        handler.removeCallbacks(this);
        locationManager.removeUpdates(this);
        mCallBack.onResponse(new Task(location, null, true));
    }

    /**
     * implement [{@link LocationListener}]
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Empty
    }

    /**
     * implement [{@link LocationListener}]
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        //Reset listener
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1, 0, LocationHelper.this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1, 0, LocationHelper.this);
    }

    /**
     * implement [{@link LocationListener}]
     */
    @Override
    public void onProviderDisabled(String provider) {
        locationManager.removeUpdates(this);
        mCallBack.onResponse(new Task(null, new Throwable(
                "Gps was disable while loading"), false));
    }

    /**
     * implement [{@link Runnable}]
     * Control the time out of current request
     */
    @Override
    public void run() {
        locationManager.removeUpdates(this);
        mCallBack.onResponse(new Task(null, new Throwable("Time out"), false));
    }

    public interface LocationCallBack {

        void onResponse(Task task);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class Task implements Serializable {

        private Location location;
        private Throwable exception;
        private boolean isSuccess;

        public Task(Location location, Throwable exception, boolean isSuccess) {
            this.location = location;
            this.exception = exception;
            this.isSuccess = isSuccess;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }
}
