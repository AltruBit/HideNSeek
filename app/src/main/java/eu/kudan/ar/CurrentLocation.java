package eu.kudan.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

class CurrentLocation implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final String debug = "CurrentLocationDebug";

    private final Activity mActivity;
    private GoogleApiClient mApiClient;
    private Context mContext;
    private OnLocationChangedListener onLocationChangedListener;

    CurrentLocation(Activity activity, Context context, OnLocationChangedListener onLocationChangedListener) {
        Log.d(debug, "CurrentLocation has been called by " + activity);
        mActivity = activity;
        mContext = context;
        this.onLocationChangedListener = onLocationChangedListener;
    }

    synchronized void apiBuild() {
        Log.d(debug, "apiBuild has been called " + mActivity);

        if (mApiClient == null) {
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    void apiStart() {
        Log.d(debug, "apiStart has been called by " + mActivity);
        mApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(debug, "onConnected has been called by " + mActivity);

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(debug, "onConnectionSuspended has been called by " + mActivity);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(debug, "onConnectionFailed has been called by " + mActivity);
    }

    void apiStop() {
        Log.d(debug, mActivity + ": apiStop has been called by " + mActivity);
        mApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(debug, "onLocationChanged has been called by " + mActivity);

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        if (mLocation != null) {
            onLocationChangedListener.onLocationChanged(mLocation);
        }
    }

    Location getCurrentLocation() {
        Log.d(debug, "getCurrentLocation has been called by " + mActivity);

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        if (mApiClient != null)
            return lastKnownLocation;
        else
            return null;
    }

}