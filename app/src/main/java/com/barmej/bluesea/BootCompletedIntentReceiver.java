package com.barmej.bluesea;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootCompletedIntentReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent i = new Intent(context, BroadcastReceiver.class);
            context.startForegroundService(i);
        }
    }
}
