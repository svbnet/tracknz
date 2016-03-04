package co.svbnet.tracknz.tracking;

import java.util.List;

/**
 * Represents a generic tracked package, which encapsulates a list of package events.
 */
public interface TrackedPackage<T extends TrackingEvent> {

    /**
     * Gets the package's code
     * @return A tracking code
     */
    String getTrackingCode();

    /**
     * Gets a user-defined label for the package.
     * @return A user-defined label, or null if none exists.
     */
    String getLabel();
    void setLabel(String label);

    /**
     * Returns the label if one exists, otherwise returns the tracking code.
     * @return A user-friendly label.
     */
    String getTitle();

    /**
     * Gets the source of the package.
     * @return A package source ID, used to lookup where the package came from.
     */
    String getSource();

    /**
     * Gets a list of events, sorted by the most recent event.
     * @return A list of events (each implementing TrackingEvent), sorted by most recent.
     */
    List<T> getEvents();

    /**
     * Gets the most recent event.
     * @return The event that occurred the most recently.
     */
    TrackingEvent getMostRecentEvent();

    String getStatus();
    String getDetailedStatus();

    /**
     * Gets if the package exists in the service's system.
     * @return If the package exists.
     */
    boolean isTracked();

    /**
     * Gets if the package has pending events.
     * @return If the package has pending events, i.e. events that the user has been notified about
     * a change in events but has not read them.
     */
    boolean hasPendingEvents();
    void setHasPendingEvents(boolean hasPendingEvents);


}
