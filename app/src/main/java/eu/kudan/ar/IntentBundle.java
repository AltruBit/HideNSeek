package eu.kudan.ar;

import android.content.Context;
import android.content.Intent;

class IntentBundle extends Intent {

    private Intent mIntent;

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
}
