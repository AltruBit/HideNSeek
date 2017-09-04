package eu.kudan.ar;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocalSetup extends AppCompatActivity implements
        View.OnClickListener ,
        OnLocationChangedListener {

    private DatabaseReference fireReference;
    private CurrentLocation mCurrentLocation;
    private IntentBundle intentBundle;

    private EditText set_area;
    private EditText set_time;
    private EditText join_game;

    private String username;
    private String joinGame;
    private double latitude;
    private double longitude;
    private int avatarAmount;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);

        mCurrentLocation = new CurrentLocation(this, this, this);

        intentBundle = new IntentBundle(getIntent());

        //Connect to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("Group");

        setLayout();

        mCurrentLocation.apiBuild();
    }

    protected void onStart() {
        mCurrentLocation.apiStart();
        super.onStart();
    }

    public void onClick(View v) {
        int i = v.getId();

        mCurrentLocation.getCurrentLocation();

        if (i == R.id.cont)
            goHide();
        else if (i == R.id.joinGameButton)
            goJoin();
    }

    protected void onStop() {
        mCurrentLocation.apiStop();
        super.onStop();
    }

    //Push data to GroupPlayHide.java
    private void pushSetUpIntent() {
        Intent intent = new Intent(this, GroupPlayHide.class);
        Bundle setupBundle = new Bundle();
        setupBundle.putString("username", username);
        setupBundle.putString("joinGame", joinGame);
        intent.putExtra("setupBundle", setupBundle);
        this.startActivity(intent);
    }

    private void goHide() {

        if (String.valueOf(set_area.getText()).equals("") || String.valueOf(set_time.getText()).equals(""))
            Toast.makeText(getBaseContext(), "Need all settings in game", Toast.LENGTH_SHORT).show();
        else {
            int area = Integer.parseInt(set_area.getText() + "");
            int time = Integer.parseInt(set_time.getText() + "");

            fireReference.child(username + "'s game").child("Players").child(username).child("Points").setValue(0);
            fireReference.child(username + "'s game").child("Game Settings").child("latitude").setValue(latitude);
            fireReference.child(username + "'s game").child("Game Settings").child("longitude").setValue(longitude);
            fireReference.child(username + "'s game").child("Game Settings").child("area").setValue(area);
            fireReference.child(username + "'s game").child("Game Settings").child("timer").setValue(time);
            fireReference.child(username + "'s game").child("Game Settings").child("avatar").setValue(avatarAmount);

            joinGame = "";
            pushSetUpIntent();
        }
    }

    private void goJoin() {

        joinGame = String.valueOf(join_game.getText());

        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (joinGame.equals(""))
                    Toast.makeText(getBaseContext(), "Need a game to join", Toast.LENGTH_SHORT).show();
                else if (!dataSnapshot.hasChild(joinGame + "'s game"))
                    Toast.makeText(getBaseContext(), "Game does not exist", Toast.LENGTH_SHORT).show();
                else {
                    fireReference.child(joinGame + "'s game").child("Players").child(username).child("Points").setValue(0);
                    pushSetUpIntent();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setLayout() {
        //Link to XML
        NumberPicker avatar_amount = (NumberPicker) findViewById(R.id.avatarAmount);
        Button goHide = (Button) findViewById(R.id.cont);
        Button goJoin = (Button) findViewById(R.id.joinGameButton);
        set_area = (EditText) findViewById(R.id.setArea);
        set_time = (EditText) findViewById(R.id.setTime);
        join_game = (EditText) findViewById(R.id.joinGameName);

        //Set Number Picker between 1-5
        avatar_amount.setMinValue(1);
        avatar_amount.setMaxValue(5);
        avatar_amount.setWrapSelectorWheel(true);

        //Store selected number for amount of Avatars
        avatar_amount.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                avatarAmount = newVal;
            }
        });

        goHide.setOnClickListener(this);
        goJoin.setOnClickListener(this);
    }

    @Override
    public void onLocationChanged(Location currentLocation) {

    }
}