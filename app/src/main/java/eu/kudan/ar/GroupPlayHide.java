package eu.kudan.ar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupPlayHide extends FragmentActivity implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String Debug = "GroupPlayHideDebug";

    private DatabaseReference fireReference;

    private GoogleApiClient apiClient;
    private GoogleMap userMap;

    private LatLng currentLatLng;

    private Button hideAvatar;
    private TextView hidden_amount;
    private TextView free_amount;

    private String username;
    private String joinGame;
    private double area;
    private double time;
    private double latitude;
    private double longitude;
    private double newLatPos;
    private double newLngPos;
    private double newLatNeg;
    private double newLngNeg;
    private double maxAvatarAmount;
    private int hiddenAmount;
    private int freeAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_play_hide);

        //Get data from LocalSetup.java
        pullSetUpIntent();

        //Connect to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();

        if (joinGame.equals(""))
            fireReference = fireDatabase.getReference("Group").child(username + "'s game");
        else
            fireReference = fireDatabase.getReference("Group").child(joinGame + "'s game");

        //Link to XML
        hideAvatar = (Button) findViewById(R.id.hideAvatar);
        Button goSeek = (Button) findViewById(R.id.seeker);
        hidden_amount = (TextView) findViewById(R.id.amountHidden);
        free_amount = (TextView) findViewById(R.id.amountFree);

        //Get game settings once
        fireReference.child("Game Settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latitude = (double) dataSnapshot.child("latitude").getValue(Long.class);
                longitude = (double) dataSnapshot.child("longitude").getValue(Long.class);
                area = (double) dataSnapshot.child("area").getValue(Long.class);
                maxAvatarAmount = (double) dataSnapshot.child("avatar").getValue(Long.class);
                time = (double) dataSnapshot.child("timer").getValue(Long.class);

                Log.d(Debug, "Finished Getting Settings");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Get data once
        fireReference.child("Players").child(username).child("Locations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hiddenAmount = (int) dataSnapshot.getChildrenCount();
                        freeAmount = (int) maxAvatarAmount - hiddenAmount;

                        free_amount.setText(String.valueOf(freeAmount));
                        hidden_amount.setText(String.valueOf(hiddenAmount));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        //Let FireBase finish getting data
        try {
            Thread.sleep(1000);
            Log.d(Debug, "Waiting for thread to finish");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Log.d(Debug, "Setting up Google Maps");

        //Setup Google Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.hideMap);
        mapFragment.getMapAsync(this);

        Log.d(Debug, "Done settings up Google Maps");

        apiBuild();

        hideAvatar.setOnClickListener(this);
        goSeek.setOnClickListener(this);
    }

    protected void onStart() {
        apiClient.connect();
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        PolygonOptions rectangle;
        userMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        else {
            userMap.setMyLocationEnabled(true);
            userMap.getUiSettings().setMyLocationButtonEnabled(false);
            userMap.getUiSettings().setScrollGesturesEnabled(false);
            userMap.getUiSettings().setZoomGesturesEnabled(false);
        }


        //Create Rectangle around user
        double areaDiv2 = area / 2;

        newLatPos = latitude + (180 / Math.PI) * (areaDiv2 / 6378137);
        newLngPos = longitude + (180 / Math.PI) * (areaDiv2 / 6378137) / Math.cos(latitude * 180 / Math.PI);
        newLatNeg = latitude - (180 / Math.PI) * (areaDiv2 / 6378137);
        newLngNeg = longitude - (180 / Math.PI) * (areaDiv2 / 6378137) / Math.cos(latitude * 180 / Math.PI);

        Log.d("Debug", "newLatPos: " + Double.toString(newLatPos));
        Log.d("Debug", "newLngPos: " + Double.toString(newLngPos));
        Log.d("Debug", "newLatNeg: " + Double.toString(newLatNeg));
        Log.d("Debug", "newLngNeg: " + Double.toString(newLngNeg));



        rectangle = new PolygonOptions()
                .add(new LatLng(newLatPos, newLngPos),
                        new LatLng(newLatNeg, newLngPos),
                        new LatLng(newLatNeg, newLngNeg),
                        new LatLng(newLatPos, newLngNeg))
                .strokeColor(Color.BLACK);

        Log.d(Debug, "Created Rectangle");

        userMap.addPolygon(rectangle);
        Log.d(Debug, "Added Rectangle");

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
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        userMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));
    }

    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.hideAvatar)
            hideAnAvatar();
        else if (i == R.id.seeker)
            pushHideIntent();
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

    /************************** Custom Functions *************************/

    //Push data to GroupPlaySeek.java
    private void pushHideIntent() {
        Intent intent = new Intent(GroupPlayHide.this, GroupPlaySeek.class);
        Bundle hideBundle = new Bundle();
        hideBundle.putDouble("newX+", newLatPos);
        hideBundle.putDouble("newX-", newLatNeg);
        hideBundle.putDouble("newY+", newLngPos);
        hideBundle.putDouble("newY-", newLngNeg);
        hideBundle.putDouble("time", time);
        hideBundle.putString("username", username);
        hideBundle.putString("joinGame", joinGame);
        intent.putExtra("hideBundle", hideBundle);
        GroupPlayHide.this.startActivity(intent);
    }

    //Get data from LocalSetup.java
    private void pullSetUpIntent() {
        Intent intent = getIntent();
        Bundle setupBundle = intent.getBundleExtra("setupBundle");
        username = setupBundle.getString("username");
        joinGame = setupBundle.getString("joinGame");
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

    private void hideAnAvatar() {
        getCurrentLocation();

        if (inRectangle()) {

            //If no free Avatars disable button
            if (freeAmount == 0) {
                Toast.makeText(getBaseContext(), "No free Avatars to hide!", Toast.LENGTH_LONG).show();
                hideAvatar.setEnabled(false);
            } else {
                freeAmount--;
                hiddenAmount++;

                free_amount.setText(String.valueOf(freeAmount));
                hidden_amount.setText(String.valueOf(hiddenAmount));

                //Add current coordinates to fireBase
                //data coordinates = new data(latitude, longitude);
                //fireReference.child("Players").child(username).child("Locations").push().setValue(coordinates);

                //Marker showing where avatar is hiding on map
                userMap.addMarker(new MarkerOptions().position(currentLatLng));

                Toast.makeText(getBaseContext(), "Hid an Avatar!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getBaseContext(), "Not in game area!", Toast.LENGTH_SHORT).show();
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