package co.svbnet.tracknz.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

import java.util.List;

import co.svbnet.tracknz.PreferenceKeys;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.activity.MainActivity;
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.PackageFlag;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;

/**
 * The app's background refresh and notification receiver.
 */
public class BackgroundNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "BackgroundReceiver";
    private static final String GROUP_KEY_PACKAGES = "group_key_packages";
    private static final int NOTIFICATION_ID = 0;
    /**
     * The icon's size in dip units
     */
    private static final int NOTIFICATION_LARGE_ICON_SIZE_DP = 48;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm triggered");
        new NotificationCheckTask(new NZPostTrackingService(), context).execute();
    }

    /**
     * Creates a round icon symbolising a package event flag.
     * @param context The context to acquire resources from.
     * @param flag The flag to draw.
     * @return A bitmap the size of {@link BackgroundNotificationReceiver#NOTIFICATION_LARGE_ICON_SIZE_DP}.
     */
    private Bitmap createNotificationLargeIcon(Context context, int flag) {
        Resources contextResources = context.getResources();
        // Get dimensions of icon appropriate for the device's screen density
        int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                NOTIFICATION_LARGE_ICON_SIZE_DP, contextResources.getDisplayMetrics());
        // Our background bitmap we're going to draw on
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Bitmap iconBitmap = BitmapFactory.decodeResource(contextResources, PackageFlag.getImageDrawableForFlag(flag));
        // Create canvas based on background bitmap
        Canvas mainCanvas = new Canvas(bitmap);
        // Create background circle, filled with an appropriate colour based on the flag
        int backgroundColor = ContextCompat.getColor(context, PackageFlag.getColorForFlag(flag));
        Paint backgroundPaint = new Paint();
        Rect backgroundRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF backgroundRectF = new RectF(backgroundRect);
        backgroundPaint.setAntiAlias(true);
        mainCanvas.drawARGB(0, 0, 0, 0);
        backgroundPaint.setColor(backgroundColor);
        // Draw circular background
        mainCanvas.drawOval(backgroundRectF, backgroundPaint);
        int cx = (bitmap.getWidth() - iconBitmap.getWidth()) >> 1;
        int cy = (bitmap.getWidth() - iconBitmap.getHeight()) >> 1;
        // Draw the icon
        mainCanvas.drawBitmap(iconBitmap, cx, cy, backgroundPaint);
        iconBitmap.recycle();
        return bitmap;
    }

    /**
     * Builds a notification for a single package. This allows for more information about the package
     * in a single notification.
     * @param context The context to create the notification with.
     * @param builder A Builder to build on.
     * @param pkg The package to notify about.
     * @return A new Notification that can be displayed.
     */
    private Notification buildSingleNotification(Context context, NotificationCompat.Builder builder, NZPostTrackedPackage pkg) {
        // Retrieve latest event
        NZPostTrackingEvent latestEvent = pkg.getMostRecentEvent();

        // Create the large icon for the notification based on the latest event flag
        Bitmap largeIcon = createNotificationLargeIcon(context, latestEvent.getFlag());

        // Begin building it
        builder.setLargeIcon(largeIcon)
                .setContentTitle(pkg.getTitle())
                .setContentText(latestEvent.getDescription())
                .setWhen(latestEvent.getDate().getTime());

        // Create the intent for the package info activity
        Intent packageInfoIntent = new Intent(context, MainActivity.class);
        packageInfoIntent.putExtra(MainActivity.CURRENT_PACKAGE, pkg);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, packageInfoIntent, PendingIntent.FLAG_ONE_SHOT));

        // Build notification
        return builder.build();
    }

    /**
     * Builds a notification for many packages. This shows a simple title and message, but can be
     * pulled down to reveal which packages were updated.
     * @param context The context to create the notification with.
     * @param builder A Builder to build on.
     * @param packages The packages to notify about.
     * @return A new Notification that can be displayed.
     */
    private Notification buildMultipleNotification(Context context, NotificationCompat.Builder builder, List<NZPostTrackedPackage> packages) {
        // Inbox style is presented as a list of items which is revealed when the user pulls down
        // on the notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (NZPostTrackedPackage item : packages) {
            inboxStyle.addLine(String.format("%s: %s", item.getTitle(), item.getMostRecentEvent().getDescription()));
        }

        // Set content text depending on events size
        String contentText;
        if (packages.size() == 2) {
            contentText = context.getString(R.string.notif_text_packages_two,
                    packages.get(0).getTitle(), packages.get(1).getTitle());
        } else {
            contentText = context.getString(R.string.notif_text_packages_more,
                    packages.get(0).getTitle(), packages.get(1).getTitle(), packages.size() - 2);
        }

        // Build notification
        builder.setNumber(packages.size())
                .setContentTitle(context.getString(R.string.notif_multiple_title))
                .setContentText(contentText)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_ONE_SHOT))
                .setStyle(inboxStyle);
        return builder.build();
    }

    /**
     * Builds a notification based on a list of packages, first setting defaults specified in code
     * and shared preferences.
     * @param context The context to use.
     * @param packages The packages to notify about.
     * @return A notification that can be displayed.
     */
    private Notification buildNotification(Context context, List<NZPostTrackedPackage> packages) {
        // Build notification with rudimentary options
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_notification_logo)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setGroup(GROUP_KEY_PACKAGES);

        // Apply notification alert preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int defaults = 0;
        boolean vibrate = sp.getBoolean(PreferenceKeys.NOTIFICATIONS_VIBRATE, true);
        boolean sound = sp.getBoolean(PreferenceKeys.NOTIFICATIONS_SOUND, true);
        boolean led = sp.getBoolean(PreferenceKeys.NOTIFICATIONS_LIGHT, true);
        if (vibrate) defaults |= Notification.DEFAULT_VIBRATE;
        if (sound) defaults |= Notification.DEFAULT_SOUND;
        if (led) defaults |= Notification.DEFAULT_LIGHTS;
        builder.setDefaults(defaults);

        // Call appropriate notification building method
        if (packages.size() == 1) {
            return buildSingleNotification(context, builder, packages.get(0));
        } else {
            return buildMultipleNotification(context, builder, packages);
        }
    }

    private class NotificationCheckTask extends PackageUpdateTask {

        public NotificationCheckTask(NZPostTrackingService service, Context context) {
            super(service, context, null);
        }

        @Override
        protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
            try {
                super.onPostExecute(trackedPackages);
            } finally {
                db.close();
            }
        }

        @Override
        protected void onPackagesInserted(List<NZPostTrackedPackage> updatedPackages) {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, buildNotification(context, updatedPackages));
        }
    }

}
