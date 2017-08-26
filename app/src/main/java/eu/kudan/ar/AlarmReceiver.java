package eu.kudan.ar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlarmReceiver extends BroadcastReceiver {

    private Long points;

    @Override
    public void onReceive(Context context, Intent intent) {
        String username = intent.getStringExtra("username");
        final long millis = intent.getLongExtra("millis", 0);

        //Setup connection to FireBase
        final FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference fireReference = fireDatabase.getReference("World").child(username);


        fireReference.child("Hiding Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fireReference.child("Points").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fireReference.child("Hiding Locations").child(String.valueOf(millis)).removeValue();

                        points = (Long) dataSnapshot.getValue();
                        points +=100;

                        fireReference.child("Points").setValue(points);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
