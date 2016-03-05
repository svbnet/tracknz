package co.svbnet.tracknz.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.activity.MainActivity;
import co.svbnet.tracknz.activity.PackageInfoActivity;
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.util.PackageFlagUtil;

/**
 * The app's background refresh and notification receiver.
 */
public class BackgroundNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "BackgroundReceiver";
    private static final String GROUP_KEY_PACKAGES = "group_key_packages";


    private Bitmap createNotificationLargeIcon(Context context, int flag) {
        Resources contextResources = context.getResources();
        // Our background bitmap we're going to draw on
        Bitmap bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);
        Bitmap iconBitmap = BitmapFactory.decodeResource(contextResources, PackageFlagUtil.getImageDrawableForFlag(flag));
        // Create canvas based on background bitmap
        Canvas mainCanvas = new Canvas(bitmap);
        // Create background circle, filled with an appropriate colour based on the flag
        int backgroundColor = ContextCompat.getColor(context, PackageFlagUtil.getColorForFlag(flag));
        Paint backgroundPaint = new Paint();
        Rect backgroundRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF backgroundRectF = new RectF(backgroundRect);
        backgroundPaint.setAntiAlias(true);
        mainCanvas.drawARGB(0, 0, 0, 0);
        backgroundPaint.setColor(backgroundColor);
        mainCanvas.drawOval(backgroundRectF, backgroundPaint);
        int cx = (bitmap.getWidth() - iconBitmap.getWidth()) >> 1;
        int cy = (bitmap.getWidth() - iconBitmap.getHeight()) >> 1;
        mainCanvas.drawBitmap(iconBitmap, cx, cy, backgroundPaint);
        iconBitmap.recycle();
        return bitmap;
    }

    private Notification buildSingleNotification(Context context, NZPostTrackedPackage pkg) {
        // Retrieve latest event
        NZPostTrackingEvent latestEvent = pkg.getMostRecentEvent();
        // Create the large icon for the notification based on the latest event flag
        Bitmap largeIcon = createNotificationLargeIcon(context, latestEvent.getFlag());
        // Begin building it
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_notification_logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(pkg.getTitle())
                .setContentText(latestEvent.getDescription())
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(GROUP_KEY_PACKAGES)
                .setWhen(latestEvent.getDate().getTime());

        // Create the intent for the package info activity
        Intent packageInfoIntent = new Intent(context, PackageInfoActivity.class);
        packageInfoIntent.putExtra(PackageInfoActivity.PACKAGE_PARCEL, pkg);
        // Create a navigation stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PackageInfoActivity.class);
        stackBuilder.addNextIntent(packageInfoIntent);
        // Get the pending intent from the stack
        PendingIntent packageInfoPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.setContentIntent(packageInfoPendingIntent);
        // Build notification and return
        return notificationBuilder.build();
    }

    private Notification buildMultipleNotification(Context context, List<NZPostTrackedPackage> packages) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (NZPostTrackedPackage item : packages) {
            inboxStyle.addLine(String.format("%s: %s", item.getTitle(), item.getMostRecentEvent().getDescription()));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setNumber(packages.size())
                .setSmallIcon(R.drawable.ic_stat_notification_logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notif_text_packages))
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_ONE_SHOT))
                .setStyle(inboxStyle)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(GROUP_KEY_PACKAGES)
                .setColor(ContextCompat.getColor(context, R.color.primary));
        return builder.build();
    }

    private Notification buildNotification(Context context, List<NZPostTrackedPackage> packages) {
        if (packages.size() == 1) {
            return buildSingleNotification(context, packages.get(0));
        } else {
            return buildMultipleNotification(context, packages);
        }
    }

    private class NotificationCheckTask extends PackageUpdateTask {

        public NotificationCheckTask(NZPostTrackingService service, Context context) {
            super(service, context, null, null);
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
            notificationManager.notify(0, buildNotification(context, updatedPackages));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm triggered");
        new NotificationCheckTask(new NZPostTrackingService(), context).execute();
    }

}
