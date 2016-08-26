package co.svbnet.tracknz.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;

/**
 * An {@link AsyncTask} that adds and updates packages.
 */
public abstract class PackageUpdateTask extends AsyncTask<Void, Void, List<NZPostTrackedPackage>> {

    private static final String TAG = PackageUpdateTask.class.getName();

    protected NZPostTrackingService service;
    protected Context context;
    protected TrackingDB db;

    private Exception error;
    private List<String> codesToRetrieve = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param context The context to run in.
     * @param dbInstance A previously created DB instance. If null, a new instance will be created from the context supplied.
     */
    public PackageUpdateTask(NZPostTrackingService service, Context context, TrackingDB dbInstance) {
        this.service = service;
        this.context = context;
        this.db = dbInstance == null ? new TrackingDB(context) : dbInstance;
    }

    @Override
    protected void onPreExecute() {
        db.open();
        codesToRetrieve = db.findAllPackageCodes();
        if (codesToRetrieve.size() == 0) {
            Log.i(TAG, "No codes to add or update!");
            cancel(true);
        }
    }

    @Override
    protected List<NZPostTrackedPackage> doInBackground(Void... params) {
        try {
            Log.i(TAG, "Preparing to update codes...");
            return service.retrievePackages(codesToRetrieve);
        } catch (Exception e) {
            error = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
        if (trackedPackages == null) {
            if (error != null) {
                Log.e(TAG, "onPostExecute error", error);
                onError(error);
            } else {
                Log.wtf(TAG, "onPostExecute: trackedPackages AND error are null!");
            }
            return;
        }
        List<NZPostTrackedPackage> updatedPackages = new ArrayList<>();
        List<NZPostTrackedPackage> packagesWithErrors = new ArrayList<>();

        for (NZPostTrackedPackage trackedPackage : trackedPackages) {
            if (trackedPackage.getErrorCode() != null) {
                packagesWithErrors.add(trackedPackage);
                continue;
            }

            // To check if a package has been updated, compare the latest event
            NZPostTrackingEvent storedEvent = db.findLatestEventForPackage(trackedPackage.getTrackingCode());
            NZPostTrackingEvent recentEvent = trackedPackage.getMostRecentEvent();

            // Also get the label
            String label = db.getLabel(trackedPackage.getTrackingCode());
            trackedPackage.setLabel(label);

            if ((storedEvent == null && recentEvent != null) || !storedEvent.equals(recentEvent)) {
                updatedPackages.add(trackedPackage);
                trackedPackage.setHasPendingEvents(true);
                db.updatePackage(trackedPackage);
            }
        }

        if (packagesWithErrors.size() > 0) {
            onPackageError(packagesWithErrors);
        }

        if (updatedPackages.size() > 0) {
            onPackagesInserted(updatedPackages);
        }

    }

    /**
     * Invoked when an exception was thrown while retrieving packages.
     * @param error The thrown exception.
     */
    protected void onError(Exception error) { }

    /**
     * Invoked when there is an error with one or more packages.
     * @param packagesWithErrors The packages containing errors.
     */
    protected void onPackageError(List<NZPostTrackedPackage> packagesWithErrors) { }

    /**
     * Invoked after packages have been inserted into the database. Never called if there is an error.
     * @param updatedPackages New and updated packages.
     */
    protected void onPackagesInserted(List<NZPostTrackedPackage> updatedPackages) { }
}
