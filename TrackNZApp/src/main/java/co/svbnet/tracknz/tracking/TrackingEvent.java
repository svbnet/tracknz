package co.svbnet.tracknz.tracking;

import java.util.Date;

/**
 * Represents a generic package event.
 */
public interface TrackingEvent {

    String getParentPackage();

    @TrackingFlag.TrackingFlagDef int getFlag();

    String getDescription();

    Date getDate();
}
