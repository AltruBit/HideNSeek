package eu.kudan.ar;

import android.content.Context;
import android.content.Intent;

class IntentBundle extends Intent {

    private Intent mIntent;

    IntentBundle(Intent intent) {
        mIntent = intent;
    }

    Intent getIntent() {
        return mIntent;
    }

    void setIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }
}
