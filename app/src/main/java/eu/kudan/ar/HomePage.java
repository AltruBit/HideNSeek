package eu.kudan.ar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity implements View.OnClickListener {

    private static final String debug = "HomePageDebug";
    private static final int MAX_AVATAR_AMOUNT = 3;

    private DatabaseReference fireReference;
    private IntentBundle intentBundle;

    private TextView free_amount;
    private TextView hidden_amount;
    private TextView point_amount;

    private String username;
    private int freeAmount;
    private int hiddenAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Get data from SignUp.java
        intentBundle = new IntentBundle(getIntent());
        username = intentBundle.getUserName();

        fireBaseSetup();
        layoutSetup();
    }

    protected void onStart() {
        super.onStart();

        //Update data when someone found an avatar
        fireReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e(debug, "onChildAdded Called");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                point_amount.setText(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                Toast.makeText(getBaseContext(), "You got one Avatar back!", Toast.LENGTH_SHORT).show();

                hiddenAmount--;
                freeAmount++;

                hidden_amount.setText(String.valueOf(hiddenAmount));
                free_amount.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.e(debug, "onChildMoved Called");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(debug, "onCancelled Called");
            }
        });
    }

    protected void onRestart() {
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    /******************** Override Functions ********************/

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.bGlobalPlay)
            intentBundle.setUserName(this, GlobalMap.class, username);
        else if (i == R.id.bLocalPlay)
            intentBundle.setUserName(this, LocalSetup.class, username);
    }

    /******************** Custom Functions ********************/

    //Setup activity_home_page.xml
    private void layoutSetup() {

        //Link to XML
        Button goToWorld = (Button) findViewById(R.id.bGlobalPlay);
        Button goToGroup = (Button) findViewById(R.id.bLocalPlay);
        TextView user_name = (TextView) findViewById(R.id.userName);
        point_amount = (TextView) findViewById(R.id.pointAmount);
        free_amount = (TextView) findViewById(R.id.amountFree);
        hidden_amount = (TextView) findViewById(R.id.amountHidden);

        user_name.setText(username);
        goToWorld.setOnClickListener(this);
        goToGroup.setOnClickListener(this);
    }

    //Setup FireBase connection
    private void fireBaseSetup() {

        final FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World").child(username);

        //Get data once
        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {

            public void onDataChange(DataSnapshot dataSnapshot) {

                //Get number of hidden avatars
                hiddenAmount = (int) dataSnapshot.child("Hiding Locations").getChildrenCount();
                freeAmount = MAX_AVATAR_AMOUNT - hiddenAmount;

                Long points = (Long) dataSnapshot.child("Points").getValue();

                if (points == null) {
                    fireReference.child("Points").setValue(0);
                    points = Long.valueOf("0");
                }

                //Set number of points
                point_amount.setText(String.valueOf(points));
                hidden_amount.setText(String.valueOf(hiddenAmount));
                free_amount.setText(String.valueOf(freeAmount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(debug, "onCancelled Called");
            }
        });
    }
}