package gps.support.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Serializable;

/**
 * Support class to get current location
 * implements [{@link LocationListener}] to handle location state, {@link Runnable} for create
 * time out
 */
class GpsHelper implements LocationListener, Runnable {

    //Control time out
    private Handler handler;
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static GpsHelper mGpsHelper;
    private LocationCallBack mCallBack;
    private LocationManager locationManager;

    @MainThread
    static GpsHelper getInstance(Context context) {
        if (mGpsHelper != null) {
            return mGpsHelper;
        } else {
            mGpsHelper = new GpsHelper(context.getApplicationContext());
            return mGpsHelper;
        }
    }

    /**
     * private Constructor
     *
     * @param context a applicationContext needed
     */
    private GpsHelper(Context context) {
        mContext = context;
        handler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Get current location with time out
     *
     * @param timeOut  Time out when waiting [{@link LocationCallBack}]
     * @param callBack [{@link LocationCallBack}] response {@link Task}
     */
    @SuppressWarnings("SameParameterValue")
    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    synchronized void getCurrentLocation(long timeOut, final LocationCallBack callBack) {
        try {
            mCallBack = callBack;
            handler.postDelayed(this, timeOut);
            //check gps everyTime call this function
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(mContext);
            com.google.android.gms.tasks.Task<LocationSettingsResponse> task =
                    client.checkLocationSettings(builder.build());
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        handler.removeCallbacks(GpsHelper.this);
                        mCallBack.onResponse(new Task(null, e, false));
                    }
                }
            });
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1, 0, GpsHelper.this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1, 0, GpsHelper.this);
                }
            });
        } finally {
            locationManager.removeUpdates(this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handler.removeCallbacks(this);
        locationManager.removeUpdates(this);
        mCallBack.onResponse(new Task(location, null, true));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Empty
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        //Reset listener
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1, 0, GpsHelper.this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1, 0, GpsHelper.this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationManager.removeUpdates(this);
        mCallBack.onResponse(new Task(null, new Throwable(
                "Gps was disable while loading"), false));
    }

    //Time out runnable
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
