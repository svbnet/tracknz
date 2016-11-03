package co.svbnet.tracknz;

import android.app.Application;
import android.preference.PreferenceManager;

import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;

/**
 * The main application class for TrackNZ
 */
public class TrackNZApplication extends Application {

    private static final String TAG = "TrackNZApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Set endpoint
        if (BuildConfig.DEBUG) {
            NZPostTrackingService.setEndpoint(
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .getString("endpoint", NZPostTrackingService.getEndpoint()));
        }
        // Apply notification settings
        new BackgroundRefreshManager(this).setFromPreferences(null);
    }



}
