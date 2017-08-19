package eu.kudan.ar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private static final String debug = "SignUpDebug";

    private DatabaseReference fireReference;
    private IntentBundle intentBundle;

    private EditText setUsername;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Check to see if user has already been created or not
        SharedPreferences setUp = getSharedPreferences("name", 0);
        boolean createdAccount = setUp.getBoolean("createdAccount", false);
        username = setUp.getString("username", username);

        intentBundle = new IntentBundle(getIntent());

        //If account created then go to HomePage.java
        if (createdAccount)
            intentBundle.setUserName(this, HomePage.class, username);

        //Connect to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference();

        permissionsRequest();
        layoutSetup();
    }

    protected void onStart() {
        super.onStart();
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

        if (i == R.id.bSignUp)
            checkName();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 111: {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ) {
                    Log.d(debug, "Permission already granted");
                } else {
                    permissionsNotSelected();
                }
            }
        }
    }

    /******************** Custom Functions ********************/

    //Setup activity_sign_up.xml
    private void layoutSetup() {
        //Link to XML
        Button signUp = (Button) findViewById(R.id.bSignUp);
        setUsername = (EditText) findViewById(R.id.userEdit);

        //Sign Up Button Clicked
        signUp.setOnClickListener(this);
    }

    //Save username to database, if it does not already exist
    private void checkName() {
        username = setUsername.getText().toString().toLowerCase();

        fireReference.child("World").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Check to see if database already has username
                if (dataSnapshot.hasChild(username)) {
                    Toast.makeText(getBaseContext(), "Username already taken!", Toast.LENGTH_SHORT).show();
                }
                else {
                    fireReference.child("World").child(username).child("Points").setValue(0);

                    //Store username in cache
                    SharedPreferences firstRun = getSharedPreferences("name", 0);
                    SharedPreferences.Editor editor = firstRun.edit();
                    editor.putString("username", username);
                    editor.putBoolean("createdAccount", true);
                    editor.apply();
                    intentBundle.setUserName(SignUp.this, HomePage.class, username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(debug, "On Cancelled Called");
            }
        });
    }

    //Request permissions
    public void permissionsRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        }
    }

    //Ask user to give permission for ones that were denied
    private void permissionsNotSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle("Permissions Required");
        builder.setMessage("Please enable the requested permissions in the app settings in order to use this demo app");
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                System.exit(1);
            }
        });
        AlertDialog noInternet = builder.create();
        noInternet.show();
    }
}