package eu.kudan.ar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GlobalMap extends FragmentActivity implements
        View.OnClickListener,
        OnLocationChangedListener,
        OnMapReadyCallback {

    private static final String debug = "GlobalMapDebug";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int MAX_AVATAR_AMOUNT = 3;

    private DatabaseReference fireReference;
    private CurrentLocation mCurrentLocation;
    private IntentBundle intentBundle;

    private GoogleMap mGoogleMap;

    private Button b_hide_avatar;
    private TextView t_hidden_amount;
    private TextView t_free_amount;

    private String username;
    private int hiddenAmount;
    private int freeAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_map);

        mCurrentLocation = new CurrentLocation(this, this, this);
        mCurrentLocation.apiBuild();

        intentBundle = new IntentBundle(getIntent());
        username = intentBundle.getUserName();

        fireBaseSetup();
        layoutSetup();
    }

    protected void onStart() {
        super.onStart();
        mCurrentLocation.apiStart();

        //Update data if someone found avatar
        fireReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(debug, "onChildAdded Called");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(debug, "onChildChanged Called");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(getBaseContext(), "You have an avatar back!", Toast.LENGTH_LONG).show();

                hiddenAmount--;
                freeAmount++;

                t_hidden_amount.setText(String.valueOf(hiddenAmount));
                t_free_amount.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(debug, "onChildMoved Called");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(debug, "onCancelled Called");
            }
        });
    }

    protected void onRestart() {
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
        mCurrentLocation.apiStart();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        mCurrentLocation.apiStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    /******************** Override Functions ********************/

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.seeker)
            searchHere();
        else if (i == R.id.hideAvatar) {
            hideHere();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        else {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (mGoogleMap != null)
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));
    }

    /******************** Custom Functions ********************/

    //Setup activity_global_map.xml
    private void layoutSetup() {

        //Set Up Google Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.hideMap);
        mapFragment.getMapAsync(this);

        //Link to XML
        Button b_go_search = (Button) findViewById(R.id.seeker);
        b_hide_avatar = (Button) findViewById(R.id.hideAvatar);
        t_hidden_amount = (TextView) findViewById(R.id.amountHidden);
        t_free_amount = (TextView) findViewById(R.id.amountFree);

        b_go_search.setOnClickListener(this);
        b_hide_avatar.setOnClickListener(this);
    }

    //Setup FireBase connection
    private void fireBaseSetup() {

        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World").child(username).child("Hiding Locations");

        //Get data once
        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get number of hidden avatars
                hiddenAmount = (int) dataSnapshot.getChildrenCount();
                freeAmount = MAX_AVATAR_AMOUNT - hiddenAmount;

                t_hidden_amount.setText(String.valueOf(hiddenAmount));
                t_free_amount.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(debug, "onCancelled Called");
            }
        });
    }

    //Search this area, if user has one avatar hidden
    private void searchHere() {
        if (Integer.parseInt(t_hidden_amount.getText() + "") == 0)
            Toast.makeText(getBaseContext(), "Need to have at least one Avatar hidden on map!", Toast.LENGTH_LONG).show();
        else
            intentBundle.setUserName(this, SearchHere.class, username);
    }

    //Hide in this area, if user has avatars to hide
    private void hideHere() {
        if (freeAmount == 0) {
            Toast.makeText(getBaseContext(), "No Avatars to hide!", Toast.LENGTH_SHORT).show();
            b_hide_avatar.setEnabled(false);
        }
        else
            intentBundle.setUserName(this, HideHere.class, username);
    }
}