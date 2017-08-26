package eu.kudan.ar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlarmReceiver extends BroadcastReceiver {

    private Long points;
    private DatabaseReference fireReference;

    @Override
    public void onReceive(Context context, Intent intent) {

        String username = intent.getStringExtra("username");
        final long millis = intent.getLongExtra("millis", 0);

        //Setup connection to FireBase
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        fireReference = fireDatabase.getReference("World").child(username);

        fireReference.child(String.valueOf(millis)).removeValue();

        fireReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fireReference.child("Points").setValue(points);
    }
}
