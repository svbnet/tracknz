package co.svbnet.tracknz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Closeable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import co.svbnet.tracknz.tracking.PackageFlag;
import co.svbnet.tracknz.tracking.nzpost.DateFormatUtil;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;

/**
 * Manages storage of tracked packages.
 */
public class TrackingDB implements Closeable {

    private static final String TAG = "TrackingDB";

    private TrackingDBHelper dbHelper;
    private SQLiteDatabase db;

    public TrackingDB(Context context) {
        dbHelper = new TrackingDBHelper(context);
    }

    /**
     * Opens a connection to the database
     */
    public void open() {
        db = dbHelper.getWritableDatabase();
        Log.i(TAG, "New DB connection opened");
    }

    /**
     * Closes the connection to the database, if one exists
     */
    public void close() {
        if (db != null) {
            db.close();
            Log.i(TAG, "DB connection closed");
        }
    }

    /**
     * Opens DB if local db instance is null. Must be called before every query.
     */
    private void openIfNotOpened() {
        if (db == null) open();
    }

    /**
     * Inserts a package into the database
     * @param package_ The package to insert
     */
    public void insertPackage(NZPostTrackedPackage package_) {
        openIfNotOpened();
        ContentValues values = createValuesForPackage(package_);
        db.insertOrThrow(TrackingDBHelper.TBL_TRACKED_PACKAGES, null, values);
        Log.d(TAG, "insertPackage: " + package_.getTrackingCode());
        if(package_.getEvents() != null && package_.getEvents().size() > 0) {
            insertEvents(package_.getTrackingCode(), package_.getEvents());
        }
    }

    /**
     * Inserts package events into the database.
     * @param packageCode The package code of the events.
     * @param events The events to insert.
     */
    public void insertEvents(String packageCode, List<NZPostTrackingEvent> events) {
        openIfNotOpened();
//        db.delete(TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS, "package = ?", new String[]{packageCode});
//        Log.d(TAG, "insertEvents: delete for " + packageCode);
        for (NZPostTrackingEvent event : events) {
            event.setParentPackage(packageCode);
            db.insertOrThrow(TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS, null, createValuesForEvent(event));
        }
        Log.d(TAG, "insertEvents: inserted " + events.size());
    }

    /**
     * Creates a set of content values appropriate for insertion into the database.
     * @param package_ The package to create content values for
     * @return A ContentValues object representing the package
     */
    private ContentValues createValuesForPackage(NZPostTrackedPackage package_) {
        ContentValues values = new ContentValues();
        values.put("code", package_.getTrackingCode());
        values.put("label", package_.getLabel());
        values.put("source", package_.getSource());
        values.put("short_description", package_.getStatus());
        values.put("detailed_description", package_.getDetailedStatus());
        values.put("has_pending_events", package_.hasPendingEvents());
        return values;
    }

    /**
     * Creates a ContentValues appropriate for insertion into the database.
     * @param event The event to create content values for
     * @return A ContentValues object representing the event
     */
    private ContentValues createValuesForEvent(NZPostTrackingEvent event) {
        ContentValues values = new ContentValues();
        values.put("package", event.getParentPackage());
        values.put("flag", event.getFlag());
        values.put("description", event.getDescription());
        values.put("datetime", DateFormatUtil.FORMAT.format(event.getDate()));
        return values;
    }

    /**
     * Creates a package from an ordered cursor when selecting all columns.
     * @param cur Cursor returned from query.
     * @return A package set from cursor values.
     */
    private NZPostTrackedPackage packageFromOrderedCursor(Cursor cur) {
        NZPostTrackedPackage trackedPackage = new NZPostTrackedPackage();
        trackedPackage.setTrackingCode(cur.getString(0));
        trackedPackage.setLabel(cur.getString(1));
        trackedPackage.setSource(cur.getString(2));
        trackedPackage.setShortDescription(cur.getString(3));
        trackedPackage.setDetailedDescription(cur.getString(4));
        trackedPackage.setHasPendingEvents(cur.getInt(5) != 0);
        return trackedPackage;
    }

    /**
     * Creates an event from an ordered cursor when selecting all columns.
     * @param cur Cursor returned from query.
     * @return An event set from cursor values.
     */
    private NZPostTrackingEvent eventFromOrderedCursor(Cursor cur) {
        NZPostTrackingEvent NZPostTrackingEvent = new NZPostTrackingEvent();
        NZPostTrackingEvent.setParentPackage(cur.getString(0));
        NZPostTrackingEvent.setFlag(cur.getInt(1));
        NZPostTrackingEvent.setDescription(cur.getString(2));
        try {
            NZPostTrackingEvent.setDate(DateFormatUtil.FORMAT.parse(cur.getString(3)));
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return NZPostTrackingEvent;
    }

    /**
     * Retrieves a package by the code specified.
     * @param code The code to look up
     * @return The package corresponding to the code (without events), or null if none is found.
     */
    public NZPostTrackedPackage findPackage(String code) {
        openIfNotOpened();
        Cursor cur = db.query(
                TrackingDBHelper.TBL_TRACKED_PACKAGES,
                new String[] { "*" },
                "code = ?",
                new String[] { code },
                null, null, null, null);
        Log.d(TAG, "findPackage: found " + cur.getCount() + " for " + code);
        if (cur.getCount() == 0) {
            return null;
        }
        try {
            cur.moveToFirst();
            return packageFromOrderedCursor(cur);
        } finally {
            cur.close();
        }
    }

    /**
     * Retrieves all packages from the DB, along with their events.
     * @return An {@link List} of {@link NZPostTrackedPackage}.
     */
    public List<NZPostTrackedPackage> findAllPackages() {
        openIfNotOpened();
        ArrayList<NZPostTrackedPackage> trackedPackages = new ArrayList<>();
        Cursor cur = db.query(
                TrackingDBHelper.TBL_TRACKED_PACKAGES,
                new String[] { "*" },
                null, null, null, null, null);
        Log.d(TAG, "findAllPackages: found " + cur.getCount());
        try {
            if (!cur.moveToFirst()) {
                return trackedPackages;
            }
            while (!cur.isAfterLast()) {
                NZPostTrackedPackage trackedPackage = packageFromOrderedCursor(cur);
                trackedPackage.setEvents(findEventsForPackage(trackedPackage.getTrackingCode()));
                trackedPackages.add(trackedPackage);
                cur.moveToNext();
            }
        } finally {
            cur.close();
        }
        return trackedPackages;
    }

    /**
     * Retrieves only the codes of each package in the database.
     * @return A string list of package codes.
     */
    public List<String> findAllPackageCodes() {
        openIfNotOpened();
        List<String> codes = new ArrayList<>();
        Cursor cur = db.query(
                TrackingDBHelper.TBL_TRACKED_PACKAGES,
                new String[] { "code" },
                null, null, null, null, null
        );
        Log.d(TAG, "findAllPackageCodes: found " + cur.getCount());
        try {
            if (!cur.moveToFirst()) {
                return codes;
            }
            while (!cur.isAfterLast()) {
                codes.add(cur.getString(0));
                cur.moveToNext();
            }
        } finally {
            cur.close();
        }
        return codes;
    }

    /**
     * Retrieves all events for a package, sorted by descending date.
     * @param packageCode The package code
     * @return An {@link ArrayList} of {@link NZPostTrackedPackage}.
     */
    public List<NZPostTrackingEvent> findEventsForPackage(String packageCode) {
        openIfNotOpened();
        ArrayList<NZPostTrackingEvent> events = new ArrayList<>();
        Cursor cur = db.query(
                TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS,
                new String[] { "*" },
                "package = ?",
                new String[] { packageCode },
                null, null, "datetime DESC");
        Log.d(TAG, "findEventsForPackage: found " + cur.getCount() + " for " + packageCode);
        try {
            if (!cur.moveToFirst()) {
                return events;
            }
            while (!cur.isAfterLast()) {
                NZPostTrackingEvent event = eventFromOrderedCursor(cur);
                events.add(event);
                cur.moveToNext();
            }
        } finally {
            cur.close();
        }
        return events;
    }

    /**
     * Returns the latest chronological event for a package.
     * @param packageCode The code to find.
     * @return An event, or null if none are found.
     */
    public NZPostTrackingEvent findLatestEventForPackage(String packageCode) {
        openIfNotOpened();
        Cursor cur = db.query(
                TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS,
                new String[] { "*" },
                "package = ?",
                new String[] { packageCode },
                null, null, "datetime DESC", "0, 1");
        try {
            if (!cur.moveToFirst()) {
                Log.i(TAG, "findLatestEvent: No event found for " + packageCode);
                return null;
            }
            return eventFromOrderedCursor(cur);
        } finally {
            cur.close();
        }
    }

    /**
     * Sets a user-defined label for the package, local to the device
     * @param packageCode The package to update
     * @param name The new name to set
     */
    public void updatePackageLabel(String packageCode, String name) {
        openIfNotOpened();
        ContentValues values = new ContentValues();
        values.put("label", name);
        db.update(TrackingDBHelper.TBL_TRACKED_PACKAGES, values, "code = ?", new String[]{packageCode});
    }

    /**
     * Retrieves a package's label by code.
     * @param code The package's code.
     * @return The package's label, or null if it doesn't exist.
     */
    public String getLabel(String code) {
        Cursor cur = db.query(TrackingDBHelper.TBL_TRACKED_PACKAGES,
                new String[] {"label"}, "code = ?", new String[]{code}, null, null, null);
        try {
            if (!cur.moveToFirst()) {
                return null;
            }
            return cur.getString(0);
        } finally {
            cur.close();
        }
    }

    /**
     * Updates a stored package by code, sets has_pending_events to true and inserts any new events.
     * @param trackedPackage The package to update, with new values and events.
     */
    public void updatePackage(NZPostTrackedPackage trackedPackage) {
        openIfNotOpened();
        ContentValues values = new ContentValues();
        values.put("short_description", trackedPackage.getStatus());
        values.put("detailed_description", trackedPackage.getDetailedStatus());
        values.put("has_pending_events", true);
        db.update(TrackingDBHelper.TBL_TRACKED_PACKAGES, values, "code = ?", new String[]{trackedPackage.getTrackingCode()});
        Log.d(TAG, "updatePackage: updated " + trackedPackage.getTrackingCode());
        insertEvents(trackedPackage.getTrackingCode(), trackedPackage.getEvents());
    }

    /**
     * Sets the has_pending_events column of a package to false
     * @param code You know what this is.
     */
    public void clearPendingEvents(String code) {
        openIfNotOpened();
        ContentValues values = new ContentValues(1);
        values.put("has_pending_events", false);
        db.update(TrackingDBHelper.TBL_TRACKED_PACKAGES, values, "code = ?", new String[]{code});
    }

    /**
     * Gets every package code who's latest event is {@link PackageFlag#FLAG_DELIVERY_COMPLETE}.
     * @return a string list of package codes.
     */
    public List<String> getDeliveredPackageCodes() {
        openIfNotOpened();
        // Select all distinct events which have a flag of delivery complete
        Cursor cur = db.query(true,
                TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS,
                new String[]{"package"},
                "flag = ?", new String[]{Integer.toString(PackageFlag.FLAG_DELIVERY_COMPLETE)},
                null, null, null, null);
        List<String> codes = new ArrayList<>();
        try {
            if (!cur.moveToFirst()) {
                return codes;
            }
            while (!cur.isAfterLast()) {
                codes.add(cur.getString(0));
                cur.moveToNext();
            }
        } finally {
            cur.close();
        }
        return codes;
    }

    /**
     * Deletes a package and its events.
     * @param packageCode The package to delete.
     */
    public void deletePackage(String packageCode) {
        openIfNotOpened();
        db.delete(TrackingDBHelper.TBL_TRACKED_PACKAGES, "code = ?", new String[]{packageCode});
        Log.d(TAG, "deletePackage: deleted package " + packageCode);
        db.delete(TrackingDBHelper.TBL_TRACKED_PACKAGE_EVENTS, "package = ?", new String[]{packageCode});
        Log.d(TAG, "deletePackage: delete events for " + packageCode);
    }

    /**
     * Drops the entire database then recreates it.
     */
    public void reset() {
        openIfNotOpened();
        dbHelper.recreate(db);
    }

}
