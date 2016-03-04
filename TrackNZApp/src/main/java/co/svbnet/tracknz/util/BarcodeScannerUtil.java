package co.svbnet.tracknz.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by Joe on 26/09/2015.
 */
public class BarcodeScannerUtil {
    public static final String ZXING_SCAN_ACTIVITY_NAME = "com.google.zxing.client.android.SCAN";
    public static final String EXTRA_SCAN_RESULT = "SCAN_RESULT";

    public static boolean isBarcodeScannerInstalled(PackageManager pm) {
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(BarcodeScannerUtil.ZXING_SCAN_ACTIVITY_NAME), 0);
        return activities.size() > 0;
    }
}
