package eu.kudan.ar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomePageTAG";
    private static final int MAX_AVATAR_AMOUNT = 3;

    private DatabaseReference fireReference;
    private FirebaseAuth fireAuth;
    private FirebaseUser fireUser;

    private TextView freeTextView;
    private TextView hiddenTextView;
    private TextView pointTextView;
    private TextView userTextView;

    private Long points;
    private int freeAmount;
    private int hiddenAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        initialUI();
        fireAuth = FirebaseAuth.getInstance();
    }

    protected void onStart() {
        super.onStart();

        fireUser = fireAuth.getCurrentUser();

        if (fireUser != null) {
            fireBaseSetup();
            updateFireBase();
        }
        else {
            Log.d(TAG, "User not logged in!");
        }
    }

    /******************** Override Functions ********************/

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.bGlobalPlay) {
            Intent intent = new Intent(this, GlobalMap.class);
            this.startActivity(intent);
        }
        else if (i == R.id.bLocalPlay) {
            Intent intent = new Intent(this, LocalSetup.class);
            this.startActivity(intent);
        }
    }

    /******************** Custom Functions ********************/

    // Setup activity_home_page.xml
    private void initialUI() {

        // Text Views
        pointTextView = (TextView) findViewById(R.id.pointAmount);
        freeTextView = (TextView) findViewById(R.id.amountFree);
        hiddenTextView = (TextView) findViewById(R.id.amountHidden);
        userTextView = (TextView) findViewById(R.id.userName);

        // Buttons
        findViewById(R.id.bGlobalPlay).setOnClickListener(this);
        findViewById(R.id.bLocalPlay).setOnClickListener(this);
    }

    private void updateUI() {

        fireUser = fireAuth.getCurrentUser();

        if (fireUser != null) {
            userTextView.setText(fireUser.getEmail());
            pointTextView.setText(String.valueOf(points));
            hiddenTextView.setText(String.valueOf(hiddenAmount));
            freeTextView.setText(String.valueOf(freeAmount));
        }
        else {
            Log.d(TAG, "User not logged in");
        }
    }

    private void updateFireBase() {
        // Update data when someone found an avatar
        fireReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded Called");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged Called");
                pointTextView.setText(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved Called");

                Toast.makeText(getBaseContext(), "You got one Avatar back!", Toast.LENGTH_SHORT).show();

                hiddenAmount--;
                freeAmount++;

                updateUI();
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

    // Setup FireBase connection
    private void fireBaseSetup() {
        fireReference = FirebaseDatabase.getInstance().getReference("World").child(fireUser.getUid());

        // Get data once
        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {

            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get number of hidden avatars
                hiddenAmount = (int) dataSnapshot.child("Hiding Locations").getChildrenCount();
                freeAmount = MAX_AVATAR_AMOUNT - hiddenAmount;

                points = (Long) dataSnapshot.child("Points").getValue();

                if (points == null) {
                    fireReference.child("Points").setValue(0);
                    points = Long.valueOf("0");
                }

                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled Called");
            }
        });
    }


}