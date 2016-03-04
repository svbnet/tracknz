package co.svbnet.tracknz;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The main application class for TrackNZ
 */
public class TrackNZApplication extends Application {

    private static final String TAG = "TrackNZApplication";

    private static PendingIntent alarmPendingIntent;

    public static final String INTENT_START_ALARM = "co.svbnet.tracknz.START_ALARM";

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable notifications on startup
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, false)) {
            enableBackgroundRefresh(preferences);
        }
    }

    public void enableBackgroundRefresh(SharedPreferences preferences) {
        long interval = Long.parseLong(preferences.getString(PreferenceKeys.NOTIFICATIONS_INTERVAL, "-1"));
        if (interval == -1) interval = AlarmManager.INTERVAL_HALF_HOUR;
        if (alarmPendingIntent == null) {
            Intent alarmIntent = new Intent(INTENT_START_ALARM);
            alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        } else {
            disableBackgroundRefresh();
        }
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, interval, alarmPendingIntent);
        Log.i(TAG, "Background refresh is enabled");
        Log.d(TAG, "Interval is " + interval);
    }

    public void disableBackgroundRefresh() {
        if (alarmPendingIntent == null) return;
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);
        alarmPendingIntent = null;
        Log.i(TAG, "Background refresh is disabled");
    }

}
