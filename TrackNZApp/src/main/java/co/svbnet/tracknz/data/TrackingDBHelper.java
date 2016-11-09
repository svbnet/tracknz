package co.svbnet.tracknz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Implementation of SQLiteOpenHelper for our tracking database
 */
public class TrackingDBHelper extends SQLiteOpenHelper {
    private static final String TAG = TrackingDBHelper.class.getName();

    public static final String DB_NAME = "tracking";
    public static final int DB_VERSION = 3;

    public static final String TBL_TRACKED_PACKAGES = "tracked_packages";
    public static final String TBL_TRACKED_PACKAGE_EVENTS = "tracked_package_events";

    private static final String[] CREATE_SQL = new String[] {
            "CREATE TABLE " + TBL_TRACKED_PACKAGES + "(" +
                "code TEXT NOT NULL, " +
                "label TEXT, " +
                "source TEXT, " +
                "short_description TEXT, " +
                "detailed_description TEXT, " +
                "has_pending_events INTEGER NOT NULL" +
            "); ",
            //"CREATE INDEX tracked_packages_index ON " + TBL_TRACKED_PACKAGES + " (code);",
            "CREATE TABLE " + TBL_TRACKED_PACKAGE_EVENTS + "(" +
                        "package TEXT NOT NULL, " +
                        "flag INTEGER NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "datetime TEXT NOT NULL," +
                        "location TEXT" +
                    ");",
    };

    private static final String[] DROP_SQL = new String[] {
            "DROP TABLE " + TBL_TRACKED_PACKAGES,
            "DROP TABLE " + TBL_TRACKED_PACKAGE_EVENTS,
    };

    public TrackingDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql : CREATE_SQL) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, String.format("Upgrading DB %s from %d to %d", DB_NAME, oldVersion, newVersion));
        if (oldVersion == 1 && newVersion == 2) {
            upgrade1to2(db);
        }
        if (newVersion == 3) {
            db.execSQL("ALTER TABLE " + TBL_TRACKED_PACKAGE_EVENTS + " ADD COLUMN location TEXT;");
        }
    }

    private void upgrade1to2(SQLiteDatabase db) {
        // A small penalty
        recreate(db);
    }

    void recreate(SQLiteDatabase db) {
        for (String sql : DROP_SQL) {
            db.execSQL(sql);
        }
        onCreate(db);
    }
}
