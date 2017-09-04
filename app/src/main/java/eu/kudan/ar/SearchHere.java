package eu.kudan.ar;

import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import eu.kudan.kudan.ARAPIKey;
import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARArbiTrack;
import eu.kudan.kudan.ARGyroPlaceManager;
import eu.kudan.kudan.ARImageNode;
import eu.kudan.kudan.ARLightMaterial;
import eu.kudan.kudan.ARMeshNode;
import eu.kudan.kudan.ARModelImporter;
import eu.kudan.kudan.ARModelNode;
import eu.kudan.kudan.ARTexture2D;

public class SearchHere extends ARActivity implements
        OnLocationChangedListener,
        View.OnClickListener,
        GestureDetector.OnGestureListener {

    private static final String debug = "SearchHereDebug";

    private GestureDetectorCompat gestureDetect;
    private CurrentLocation mCurrentLocation;
    private DatabaseReference fireReference;
    private IntentBundle intentBundle;
    private Quaternion orientation;
    private ARModelNode modelNode;
    private DataSnapshot toRemove;
    private CircleOptions circle;
    private Vector3f position;
    private Data fireData;
    private FirebaseUser currentUser;

    private TextView timer;

    private Long points;


    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_here);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCurrentLocation = new CurrentLocation(this, this, this);
        mCurrentLocation.apiBuild();

        gestureDetect = new GestureDetectorCompat(this, this);

        //Get data from GlobalMap.java
        intentBundle = new IntentBundle(getIntent());

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        currentUser = authentication.getCurrentUser();

        //Setup connection to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World");

        //Kudan API Key
        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey(String.valueOf(R.string.kudan_api_key));

        layoutSetup();

        countDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                timer.setText("Timer:" + millisUntilFinished/1000);

                if (millisUntilFinished / 1000 == 10)
                    Toast.makeText(getBaseContext(), "10 seconds remaining!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                Toast.makeText(getBaseContext(), "Time is up!", Toast.LENGTH_SHORT).show();
                intentBundle.setIntent(SearchHere.this, GlobalMap.class);
            }
        }.start();

    }

    protected void onStart() {
        super.onStart();
        mCurrentLocation.apiStart();
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
        countDownTimer.cancel();
        mCurrentLocation.apiStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    /******************** Override Functions ********************/

    @Override
    public void setup() {
        addModelNode();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.clickSearch)
            search();
        else if (i == R.id.backToMap)
            intentBundle.setIntent(this, GlobalMap.class);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(debug, "onLocationChanged has been called");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetect.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        ARArbiTrack arArbiTrack = ARArbiTrack.getInstance();

        if (arArbiTrack.getIsTracking())
        {
            arArbiTrack.stop();
            points += 100;
            fireReference.child(String.valueOf(currentUser)).child("Points").setValue(points);
            toRemove.getRef().removeValue();
            intentBundle.setIntent(this, GlobalMap.class);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /******************** Custom Functions ********************/

    //Setup activity_search_here.xml
    private void layoutSetup() {
        Button searchHere = (Button) findViewById(R.id.clickSearch);
        Button bGoBack = (Button) findViewById(R.id.backToMap);
        timer = (TextView) findViewById(R.id.timer);

        searchHere.setOnClickListener(this);
        bGoBack.setOnClickListener(this);
    }

    //Create model Node for AR
    private void addModelNode() {

        // Import model
        ARModelImporter modelImporter = new ARModelImporter();
        modelImporter.loadFromAsset("ben.jet");
        modelNode = modelImporter.getNode();

        // Load model texture
        ARTexture2D texture2D = new ARTexture2D();
        texture2D.loadFromAsset("bigBenTexture.png");

        // Apply model texture to model texture material
        ARLightMaterial material = new ARLightMaterial();
        material.setTexture(texture2D);
        material.setAmbient(0.8f, 0.8f, 0.8f);

        // Apply texture material to models mesh nodes
        for (ARMeshNode meshNode : modelImporter.getMeshNodes()) {
            meshNode.setMaterial(material);
        }

        modelNode.scaleByUniform(0.25f);
    }

    //Setup model node tracking
    public void setupARAbiTrack() {

        // Create an image node to be used as a target node
        ARImageNode targetImageNode = new ARImageNode("target.png");

        // Scale and rotate the image to the correct transformation.
        targetImageNode.scaleByUniform(0.3f);
        targetImageNode.rotateByDegrees(90, 1, 0, 0);

        targetImageNode.setPosition(position);
        targetImageNode.setScale(0.5F, 0.5F, 0.5F);
        targetImageNode.setOrientation(orientation);

        // Initialise gyro placement. Gyro placement positions content on a virtual floor plane where the device is aiming.
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.initialise();

        // Initialise the arbiTracker
        ARArbiTrack arArbiTrack = ARArbiTrack.getInstance();
        arArbiTrack.initialise();

        // Set the arAbiTracker target node to the node moved by the user.
        arArbiTrack.setTargetNode(targetImageNode);

        // Add model node to world
        arArbiTrack.getWorld().addChild(modelNode);
}

    //Search are for 30 seconds
    private void search() {
        final float[] distance = new float[2];
        final double latitude = mCurrentLocation.getCurrentLocation().getLatitude();
        final double longitude = mCurrentLocation.getCurrentLocation().getLongitude();

        circle = new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(20)
                .visible(false);

        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot storeSnap : dataSnapshot.getChildren()) {

                    //Skip own username
                    if (!storeSnap.getKey().equals(currentUser)) {
                        for (DataSnapshot dataSnap : storeSnap.child("Hiding Locations").getChildren()) {
                                Log.d(debug, "Searching user:" + storeSnap.getKey());

                                fireData = dataSnap.getValue(Data.class);

                                 double markerX = fireData.getLatitude();
                                double markerY = fireData.getLongitude();

                                 Location.distanceBetween(latitude, longitude, markerX, markerY, distance);

                                 if (distance[0] < circle.getRadius()) {
                                     Toast.makeText(getBaseContext(), "Someone is around this area!", Toast.LENGTH_SHORT).show();

                                     toRemove = dataSnap;
                                     position = fireData.getPosition();
                                     orientation = fireData.getOrientation();

                                     setupARAbiTrack();
                                     startAR();
                                 } else
                                     Toast.makeText(getBaseContext(), "No one around here!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        points = (Long)storeSnap.child("Points").getValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(debug, "onCancelled Called");
            }
        });
    }

    //Start AR tracking
    public void startAR() {
        ARArbiTrack arArbiTrack = ARArbiTrack.getInstance();

        arArbiTrack.start();
        arArbiTrack.getTargetNode().setVisible(false);
    }
}