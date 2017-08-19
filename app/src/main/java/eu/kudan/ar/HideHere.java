package eu.kudan.ar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import static android.content.ContentValues.TAG;

public class HideHere extends ARActivity implements
        OnLocationChangedListener,
        View.OnClickListener {

    private static final String debug = "HideHereDebug";

    private DatabaseReference fireReference;
    private CurrentLocation mCurrentLocation;
    private IntentBundle intentBundle;
    private ARModelNode modelNode;

    private String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_here);

        mCurrentLocation = new CurrentLocation(this, this, this);
        mCurrentLocation.apiBuild();

        //Get data from GlobalMap.java
        intentBundle = new IntentBundle(getIntent());
        username = intentBundle.getUserName();

        //Setup connection to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World").child(username);

        //Kudan API key
        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey("sVmoznmKZ+4nFEHD6HoslwpC26PNuBZGHrikUwyon2BKSvza1yu2CqbSrae+pHPr1NHjhsf5pHQOZn8IEqXlqXFodGsrOJhxJANbMOdvnRLUi9/QWGqyRL9FViDmyohw6e5R7U4Ex8H7d7spLLvhfp5HFv56DgLr8c8sC2ipDtv9g1IjOTaY7UGxata3eulG2A/UkIdRv2NcotZXqan01xQUWFAislEwlGguParEYiwu11T4mqtU3dQBbfxpvxbczjdYz493YG3rAO2RHgT+5M5TJShJsz2irkNo71JD2Fzqf4AR2b4+7t1c55zKjegXzGS6Xa/rpNn9yiXUn7rUYIHNvN3cEQa9HsZiVxAV4vJgxFS+T/AxfWqKrEg1uj6xF5MsodZ2EkZ8mqliYIsxZqnFz+Re2HeWG8wvrEob0ZwRIO0TxppAemZc3HChTAPLcNt5gzeBk0oRP4wnrFAFFBDi8XjDocwTSVw++hWZb1qNHzt6bKLsMDRT057UVuuZB6M8f7EOQD79Oah0Vrx/3DUK6e9BEV8oGFNHtk1wyYEkg0i6RLhVSokGx//Qj36A4gCz3h1OjtfB0OuukbNq7xI1L/FcNQLmGYNGZwszARjGr9ESw1gVAkbQMxaV27uo/KoIq4+nR7RL8iT7t7NAaXCFIi24RR+7WGjTvKqWYjA=");

        layoutSetup();
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
        try {
            unregisterReceiver(broadcast);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }

        super.onStop();
        mCurrentLocation.apiStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Stopped service");
    }


    /******************** Override Functions ********************/

    @Override
    public void setup() {
        addModelNode();
        setupARAbiTrack();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.bHide)
            startAR();
        else if (i == R.id.hBackToMap)
            killAR();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(debug, "onLocationChanged has been called");
    }

    /******************** Custom Functions ********************/

    //Setup activity_hide_here.xml
    private void layoutSetup() {
        Button bHide = (Button) findViewById(R.id.bHide);
        Button bGoBack = (Button) findViewById(R.id.hBackToMap);

        bHide.setOnClickListener(this);
        bGoBack.setOnClickListener(this);
    }

    //Create model node for AR
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
    private void setupARAbiTrack() {

        // Create an image node to be used as a target node
        ARImageNode targetImageNode = new ARImageNode("target.png");

        // Scale and rotate the image to the correct transformation.
        targetImageNode.scaleByUniform(0.3f);
        targetImageNode.rotateByDegrees(90, 1, 0, 0);

        // Initialise gyro placement. Gyro placement positions content on a virtual floor plane where the device is aiming.
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.initialise();

        // Add target node to gyro place manager
        gyroPlaceManager.getWorld().addChild(targetImageNode);

        // Initialise the arAbiTracker
        ARArbiTrack arAbiTrack = ARArbiTrack.getInstance();
        arAbiTrack.initialise();

        // Set the arAbiTracker target node to the node moved by the user.
        arAbiTrack.setTargetNode(targetImageNode);

        // Add model node to world
        arAbiTrack.getWorld().addChild(modelNode);
        arAbiTrack.getTargetNode().setVisible(true);
    }

    //Start AR Tracking
    private void startAR() {
        double latitude = mCurrentLocation.getCurrentLocation().getLatitude();
        double longitude = mCurrentLocation.getCurrentLocation().getLongitude();

        ARArbiTrack arArbiTrack = ARArbiTrack.getInstance();
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();

        arArbiTrack.start();
        arArbiTrack.getTargetNode().setVisible(false);

        Vector3f position = gyroPlaceManager.getWorld().getPosition();
        Vector3f scale = gyroPlaceManager.getWorld().getScale();
        Quaternion orientation = gyroPlaceManager.getWorld().getOrientation();

        //Add current coordinates to fireBase
        Data mData = new Data(latitude, longitude, position, scale, orientation);
        fireReference.child("Data").push().setValue(mData);

        Toast.makeText(getBaseContext(), "Hid an Avatar!", Toast.LENGTH_LONG).show();

        intentBundle.setUserName(this, GlobalMap.class, username);
    }

    //Stop Tracking and remove data
    private void killAR() {
        ARArbiTrack.getInstance().deinitialise();
        ARGyroPlaceManager.getInstance().deinitialise();

        intentBundle.setUserName(this, GlobalMap.class, username);
    }

    private BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
        }
    }
}
