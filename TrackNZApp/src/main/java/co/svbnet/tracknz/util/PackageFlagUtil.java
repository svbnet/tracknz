package co.svbnet.tracknz.util;

import co.svbnet.tracknz.R;

/**
 * Provides drawable resources for event flags
 */
public class PackageFlagUtil {

    /**
     * Gets a drawable resource ID for the specified flag.
     * @param flag A tracking event flag constant.
     * @return A drawable resource ID of an image for the flag specified.
     */
    public static int getImageDrawableForFlag(int flag) {
        switch (flag){
            case FLAG_PICKED_UP:
                return R.drawable.ic_status_picked_up;

            case FLAG_ITEM_DEPARTED:
                return R.drawable.ic_status_in_transit;

            case FLAG_ITEM_ARRIVED:
                return R.drawable.ic_status_accepted;

            default:
                return R.drawable.ic_status_indeterminate;

            case FLAG_DELIVERY_COMPLETE:
                return R.drawable.ic_status_complete;

            case FLAG_OUT_FOR_DELIVERY:
            case FLAG_HANDOVER_DELIVERY:
                return R.drawable.ic_status_delivery;
        }
    }

    /**
     * Gets the appropriate drawable resource ID shape for the specified flag.
     * @param flag A tracking event flag constant.
     * @return A drawable shape ID (filled-in circle) for the background to complement the image.
     */
    public static int getBackgroundDrawableForFlag(int flag) {
        switch (flag) {
            case FLAG_DELIVERY_COMPLETE:
                return R.drawable.tracking_status_icon_done;

            case FLAG_OUT_FOR_DELIVERY:
            case FLAG_HANDOVER_DELIVERY:
                return R.drawable.tracking_status_icon_delivery;

            default:
                return R.drawable.tracking_status_icon;
        }
    }

    /**
     * Gets the appropriate colour for the flag.
     * @param flag A tracking event flag constant.
     * @return A colour.
     */
    public static int getColorForFlag(int flag) {
        switch (flag) {
            case FLAG_DELIVERY_COMPLETE:
                return R.color.tracking_status_done;

            case FLAG_OUT_FOR_DELIVERY:
            case FLAG_HANDOVER_DELIVERY:
                return R.color.tracking_status_delivery;

            default:
                return R.color.tracking_status_indeterminate;
        }
    }

}
