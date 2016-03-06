package co.svbnet.tracknz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.svbnet.tracknz.BackgroundRefreshManager;

/**
 * Receiver for when the system has booted in order to enable notifications at boot time
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new BackgroundRefreshManager(context).setFromPreferences(null);
    }
}
