package eu.kudan.ar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class GroupPlaySeek extends FragmentActivity implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Data fireData;

    private DatabaseReference fireReference;

    private GoogleApiClient apiClient;
    private GoogleMap userMap;

    private Button search;
    private TextView point_amount;
    private TextView time_amount;

    private String username;
    private String joinGame;
    private final float[] distance = new float[2];    //Store distance between two coordinates
    private double latitude;
    private double longitude;
    private double newLatPos;
    private double newLngPos;
    private double newLatNeg;
    private double newLngNeg;
    private double markerX;                     //marker location (latitude)
    private double markerY;                     //marker location (longitude)
    private int pointAmount;
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_play_seek);

        //Get data from GroupPlayHide.java
        pullHideIntent();

        //Setup connection to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();

        if (joinGame.equals(""))
            fireReference = fireDatabase.getReference("Group").child(username + "'s game");
        else
            fireReference = fireDatabase.getReference("Group").child(joinGame + "'s game");

        search = (Button) findViewById(R.id.search);
        point_amount = (TextView) findViewById(R.id.pointsAmount);
        time_amount = (TextView) findViewById(R.id.Timer);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.seekMap);
        mapFragment.getMapAsync(this);

        time *= 60000;

        Timer();
        apiBuild();

        search.setOnClickListener(this);
    }

    protected void onStart() {
        apiClient.connect();
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;
        PolygonOptions rectangle;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        } else {
            userMap.setMyLocationEnabled(true);
            userMap.getUiSettings().setMyLocationButtonEnabled(false);
            userMap.getUiSettings().setScrollGesturesEnabled(false);
            userMap.getUiSettings().setZoomGesturesEnabled(false);
        }

        rectangle = new PolygonOptions()
                .add(new LatLng(newLatPos, newLngPos),
                        new LatLng(newLatNeg, newLngPos),
                        new LatLng(newLatNeg, newLngNeg),
                        new LatLng(newLatPos, newLngNeg))
                .strokeColor(Color.BLACK);

        userMap.addPolygon(rectangle);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        userMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.seeker)
            search();
    }

    protected void onStop() {
        apiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /********************** Custom Functions ******************************/

    //Get data from GroupPlayHide.java
    private void pullHideIntent() {
        Intent intent = getIntent();
        Bundle hideBundle = intent.getBundleExtra("hideBundle");
        newLatPos = hideBundle.getDouble("newX+");
        newLatNeg = hideBundle.getDouble("newX-");
        newLngPos = hideBundle.getDouble("newY+");
        newLngNeg = hideBundle.getDouble("newY-");
        time = (int) hideBundle.getDouble("timer");
        username = hideBundle.getString("username");
        joinGame = hideBundle.getString("joinGame");
    }

    //Get current Latitude and Longitude
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
        }
    }

    private boolean inRectangle() {
        return ((latitude < newLatPos && latitude > newLatNeg) && (longitude > newLngPos && longitude < newLngNeg));
    }


    private void Timer() {
        new CountDownTimer(time, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes));

                time_amount.setText(String.valueOf(minutes + ":" + seconds));
            }

            @Override
            public void onFinish() {
                search.setEnabled(false);
                Toast.makeText(getBaseContext(), "Game is Over!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void search() {
        final Circle circle;

        //Check if user is in area
        if (inRectangle()) {
            getCurrentLocation();

            //Draw circle around user
            circle = userMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(10)
                    .visible(false));

            //Get data once
            fireReference.child("Players").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot storeSnap : dataSnapshot.getChildren()) {

                        if (!storeSnap.getKey().equals(username)) {
                            for (DataSnapshot dataSnap : storeSnap.child("Locations").getChildren()) {
                                fireData = dataSnap.getValue(Data.class);

                                markerX = fireData.getLatitude();
                                markerY = fireData.getLongitude();
                                Location.distanceBetween(latitude, longitude, markerX, markerY, distance);

                                //If inside radius increment points and delete coordinates
                                if (distance[0] < circle.getRadius()) {
                                    pointAmount = Integer.parseInt(point_amount.getText() + "");
                                    pointAmount += 100;

                                    fireReference.child("Players").child(username).child("Points").setValue(pointAmount);

                                    point_amount.setText(String.valueOf(pointAmount));

                                    dataSnap.getRef().removeValue();

                                    Toast.makeText(getBaseContext(), "Found " + storeSnap.getKey() + " hiding!", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(getBaseContext(), "No one is hiding in the area", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } else
            Toast.makeText(getBaseContext(), "Not in game area!", Toast.LENGTH_LONG).show();
    }

    private void apiBuild() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
}