package co.svbnet.tracknz.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import co.svbnet.tracknz.tracking.TrackedPackage;
import co.svbnet.tracknz.tracking.TrackingService;

/**
 * Created by Joe on 2/10/2015.
 */
public abstract class PackageRetrievalTask extends AsyncTask<String, Void, List<TrackedPackage>> {

    private static final String TAG = PackageRetrievalTask.class.getName();

    private Exception error;

    private TrackingService service;

    public PackageRetrievalTask(TrackingService service) {
        this.service = service;
    }

    @Override
    protected List<TrackedPackage> doInBackground(String... params) {
        try {
            Log.i(TAG, "Preparing to update codes...");
            return this.service.retrievePackages(Arrays.asList(params));
        } catch (Exception e) {
            Log.e(TAG, "Error:", e);
            error = e;
            return null;
        }
    }

    /**
     * Called when the background thread returns. The trackedPackages argument should not be trusted
     * as it will be null if an exception is thrown while retrieving codes occurs; use onException
     * or onSuccess if you need error or success callbacks.
     * @param trackedPackages The list returned from doInBackground. May be null.
     */
    @Override
    protected void onPostExecute(List<TrackedPackage> trackedPackages) {
        if (error != null) {
            onException(error);
        } else {
            onSuccess(trackedPackages);
        }
    }

    /**
     * Called when an exception occurs on the background thread after it has finished executing.
     * @param ex The thrown exception.
     */
    protected abstract void onException(Exception ex);

    /**
     * Called when tracked packages were retrieved successfully.
     * @param retrievedPackages A list of tracked packages returned from the API call.
     */
    protected abstract void onSuccess(List<TrackedPackage> retrievedPackages);
}
