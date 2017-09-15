package eu.kudan.ar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class HideHere extends ARActivity implements
        OnLocationChangedListener,
        View.OnClickListener {

    private static final String TAG = "HideHereTAG";

    private DatabaseReference fireReference;

    private CurrentLocation mCurrentLocation;

    private ARModelNode modelNode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_here);

        mCurrentLocation = new CurrentLocation(this, this, this);
        mCurrentLocation.apiBuild();

        //Kudan API key
        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey(String.valueOf(R.string.kudan_api_key));

        initialUI();
    }

    protected void onStart() {
        super.onStart();

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fireUser != null) {
            mCurrentLocation.apiStart();
            fireReference = FirebaseDatabase.getInstance().getReference("World").child(String.valueOf(fireUser.getUid()));
        }
        else
            Log.d(TAG, "User not logged in!");
    }

    protected void onResume() {
        super.onResume();
        mCurrentLocation.apiStart();
    }

    protected void onStop() {
        super.onStop();
        mCurrentLocation.apiStop();
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
        Log.d(TAG, "onLocationChanged has been called");
    }

    /******************** Custom Functions ********************/

    //Setup activity_hide_here.xml
    private void initialUI() {
        findViewById(R.id.bHide).setOnClickListener(this);
        findViewById(R.id.hBackToMap).setOnClickListener(this);
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

        long currentMillis = System.currentTimeMillis();

        //Add current coordinates to fireBase
        final Data mData = new Data(latitude, longitude, position, scale, orientation);
        fireReference.child("Hiding Locations").child(String.valueOf(currentMillis)).setValue(mData);

        Toast.makeText(getBaseContext(), "Hid an Avatar!", Toast.LENGTH_LONG).show();

        final Intent alarmIntent = new Intent(HideHere.this, AlarmReceiver.class);
        alarmIntent.putExtra("millis", currentMillis);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(HideHere.this, (int) currentMillis, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        manager.setExact(AlarmManager.RTC_WAKEUP, currentMillis + 30000, pendingIntent);

        Intent intent = new Intent(this, GlobalMap.class);
        this.startActivity(intent);
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

    //Stop Tracking and remove data
    private void killAR() {
        ARGyroPlaceManager.getInstance().deinitialise();

        Intent intent = new Intent(this, GlobalMap.class);
        this.startActivity(intent);
    }
}
