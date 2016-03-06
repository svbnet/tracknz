package co.svbnet.tracknz;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Manages enabling and disabling of the background package refresh feature.
 */
public class BackgroundRefreshManager {

    private static PendingIntent alarmPendingIntent;

    public static final String INTENT_START_ALARM = "co.svbnet.tracknz.START_ALARM";
    private static final String TAG = "BackgroundRefreshManager";

    private Context ctx;

    public BackgroundRefreshManager(Context appContext) {
        ctx = appContext;
    }

    public void enableBackgroundRefresh(SharedPreferences preferences) {
        long interval = Long.parseLong(preferences.getString(PreferenceKeys.NOTIFICATIONS_INTERVAL, "-1"));
        if (interval == -1) interval = AlarmManager.INTERVAL_HALF_HOUR;
        if (alarmPendingIntent == null) {
            Intent alarmIntent = new Intent(INTENT_START_ALARM);
            alarmPendingIntent = PendingIntent.getBroadcast(ctx, 0, alarmIntent, 0);
        } else {
            disableBackgroundRefresh();
        }
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, interval, alarmPendingIntent);
        Log.i(TAG, "Background refresh is enabled");
        Log.d(TAG, "Interval is " + interval);
    }

    public void disableBackgroundRefresh() {
        if (alarmPendingIntent == null) return;
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);
        alarmPendingIntent = null;
        Log.i(TAG, "Background refresh is disabled");
    }

    /**
     * Enables or disables background refresh based on the {@link PreferenceKeys#NOTIFICATIONS_ENABLED} preference.
     * @param pm The preference manager to use. Pass null to use the default shared preferences.
     */
    public void setFromPreferences(SharedPreferences sp) {
        SharedPreferences preferences = sp == null
                ? PreferenceManager.getDefaultSharedPreferences(ctx)
                : sp;
        if (preferences.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, false)) {
            enableBackgroundRefresh(preferences);
        } else {
            disableBackgroundRefresh();
        }
    }

}
