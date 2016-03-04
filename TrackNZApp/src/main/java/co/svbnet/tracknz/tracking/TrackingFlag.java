package co.svbnet.tracknz.tracking;

import android.support.annotation.IntDef;

/**
 * Created by Joe on 18/02/2016.
 */
public class TrackingFlag {

    public static final int FLAG_UNKNOWN = -1;
    public static final int FLAG_PICKED_UP = 'A';
    public static final int FLAG_ITEM_DEPARTED = 'B';
    public static final int FLAG_ITEM_ARRIVED = 'C';
    public static final int FLAG_ITEM_HELD_FOR_CLEARANCE = 'D';
    public static final int FLAG_DELIVERY_COMPLETE = 'F';
    public static final int FLAG_HANDOVER_DELIVERY = 'K';
    public static final int FLAG_OUT_FOR_DELIVERY = 'O';

    @IntDef({FLAG_UNKNOWN, FLAG_PICKED_UP, FLAG_ITEM_DEPARTED, FLAG_ITEM_ARRIVED,
            FLAG_ITEM_HELD_FOR_CLEARANCE, FLAG_DELIVERY_COMPLETE, FLAG_HANDOVER_DELIVERY,
            FLAG_OUT_FOR_DELIVERY})
    public @interface TrackingFlagDef {}

}
