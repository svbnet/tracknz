package co.svbnet.tracknz.util;

import android.app.Activity;
import android.content.Intent;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;

/**
 * Created by Joe on 2/12/2015.
 */
public class ShareUtil {

    public static void shareCode(Activity context, String code) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, code);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.title_share_code)));
    }

    public static void sharePackageUrl(Activity activity, NZPostTrackedPackage trackedPackage) {
        String shareUrl = "";
        if (trackedPackage.getSource().equals("nz_post")) {
            shareUrl = String.format(TrackingApi.NZP_URL_FORMAT, trackedPackage.getCode());
        } else if(trackedPackage.getSource().equals("courier_post")) {
            shareUrl = String.format(TrackingApi.CP_URL_FORMAT, trackedPackage.getCode());
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        shareIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.title_share_url)));
    }

}
