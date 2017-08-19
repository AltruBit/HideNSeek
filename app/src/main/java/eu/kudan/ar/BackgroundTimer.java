package eu.kudan.ar;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BackgroundTimer extends Service{

    private final static String TAG = "BackgroundTimerDebug";

    public static final String COUNTDOWN_BR = "eu.kudan.ar.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    CountDownTimer countdown = null;

    @Override
    public void onCreate() {
        super.onCreate();

        countdown = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                bi.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bi);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");
            }
        };

        countdown.start();
    }

    public void onDestroy() {
        Log.i(TAG, "Timer cancelled");
        countdown.cancel();
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
