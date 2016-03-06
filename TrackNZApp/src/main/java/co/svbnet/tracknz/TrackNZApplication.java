package co.svbnet.tracknz;

import android.app.Application;

/**
 * The main application class for TrackNZ
 */
public class TrackNZApplication extends Application {

    private static final String TAG = "TrackNZApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply notification settings
        new BackgroundRefreshManager(this).setFromPreferences(null);
    }



}
