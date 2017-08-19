package eu.kudan.ar;

import android.content.Context;
import android.content.Intent;

class IntentBundle {

    private Intent mIntent;

    IntentBundle() {}

    IntentBundle(Intent intent) {
        mIntent = intent;
    }

    String getUserName() {
        return mIntent.getStringExtra("username");
    }

    void setUserName(Context context, Class<?> cls, String username) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("username", username);
        context.startActivity(intent);
    }
/*
    void setUserName(Activity activity, Class<?> cls, String username) {
        Intent intent = new Intent(activity, cls);
        intent.putExtra("username", username);
        activity.startActivityForResult(intent, 101);
    }
    */
}
