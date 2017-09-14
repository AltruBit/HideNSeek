package eu.kudan.ar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
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

    private static final String TAG = "GlobalMapTAG";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int MAX_AVATAR_AMOUNT = 3;

    private DatabaseReference fireReference;
    private FirebaseUser fireUser;

    private CurrentLocation currentLocation;

    private GoogleMap googleMap;

    private Button hideButton;
    private TextView hiddenTextView;
    private TextView freeTextView;

    private int hiddenAmount;
    private int freeAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_map);

        currentLocation = new CurrentLocation(this, this, this);
        currentLocation.apiBuild();

        fireUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fireUser != null) {
            fireBaseSetup();
            layoutSetup();
        }
        else
            Log.d(TAG, "User logged out");
    }

    protected void onStart() {
        super.onStart();
        currentLocation.apiStart();

        //Update data if someone found avatar
        fireReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded Called");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged Called");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(getBaseContext(), "You have an avatar back!", Toast.LENGTH_LONG).show();

                hiddenAmount--;
                freeAmount++;

                hiddenTextView.setText(String.valueOf(hiddenAmount));
                freeTextView.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved Called");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled Called");
            }
        });
    }

    protected void onRestart() {
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
        currentLocation.apiStart();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        currentLocation.apiStop();
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
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        else {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (googleMap != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));
    }

    /******************** Custom Functions ********************/

    //Setup activity_global_map.xml
    private void layoutSetup() {

        //Set Up Google Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.hideMap);
        mapFragment.getMapAsync(this);

        //Link to XML
        Button b_go_search = (Button) findViewById(R.id.seeker);
        hideButton = (Button) findViewById(R.id.hideAvatar);
        hiddenTextView= (TextView) findViewById(R.id.amountHidden);
        freeTextView = (TextView) findViewById(R.id.amountFree);

        b_go_search.setOnClickListener(this);
        hideButton.setOnClickListener(this);
    }

    //Setup FireBase connection
    private void fireBaseSetup() {

        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World").child(String.valueOf(fireUser.getUid())).child("Hiding Locations");

        //Get data once
        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get number of hidden avatars
                hiddenAmount = (int) dataSnapshot.getChildrenCount();
                freeAmount = MAX_AVATAR_AMOUNT - hiddenAmount;

                hiddenTextView.setText(String.valueOf(hiddenAmount));
                freeTextView.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled Called");
            }
        });
    }

    //Search this area, if user has one avatar hidden
    private void searchHere() {
        if (Integer.parseInt(hiddenTextView.getText() + "") == 0)
            Toast.makeText(getBaseContext(), "Need to have at least one Avatar hidden on map!", Toast.LENGTH_LONG).show();
        else {
            Intent intent = new Intent(this, SearchHere.class);
            this.startActivity(intent);
        }
    }

    //Hide in this area, if user has avatars to hide
    private void hideHere() {
        if (freeAmount == 0) {
            Toast.makeText(getBaseContext(), "No Avatars to hide!", Toast.LENGTH_SHORT).show();
            hideButton.setEnabled(false);
        }
        else {
            Intent intent = new Intent(this, HideHere.class);
            this.startActivity(intent);
        }
    }
}