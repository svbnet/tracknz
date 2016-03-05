package co.svbnet.tracknz.tracking.nzpost;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a package in the NZ Post tracking system.
 */
public class NZPostTrackedPackage implements Parcelable {

    // This is set after the API response is parsed as a dictionary of code-package objects is returned.
    private String code;

    private String label;

    @SerializedName("source")
    private String source;

    @SerializedName("short_description")
    private String shortDescription;

    @SerializedName("detail_description")
    private String detailedDescription;

    @SerializedName("events")
    private List<NZPostTrackingEvent> events;

    @SerializedName("error_code")
    private String errorCode;

    private boolean areEventsSorted = false;

    private boolean hasPendingEvents = false;

    public NZPostTrackedPackage() {

    }

    //region Parcelable implementation
    protected NZPostTrackedPackage(Parcel in) {
        code = in.readString();
        label = in.readString();
        source = in.readString();
        shortDescription = in.readString();
        detailedDescription = in.readString();
        events = in.createTypedArrayList(NZPostTrackingEvent.CREATOR);
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(label);
        dest.writeString(source);
        dest.writeString(shortDescription);
        dest.writeString(detailedDescription);
        dest.writeTypedList(events);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NZPostTrackedPackage> CREATOR = new Creator<NZPostTrackedPackage>() {
        @Override
        public NZPostTrackedPackage createFromParcel(Parcel in) {
            return new NZPostTrackedPackage(in);
        }

        @Override
        public NZPostTrackedPackage[] newArray(int size) {
            return new NZPostTrackedPackage[size];
        }
    };
    //endregion

    private void sortEventsByDate() {
        Collections.sort(events, new Comparator<NZPostTrackingEvent>() {
            @Override
            public int compare(NZPostTrackingEvent lhs, NZPostTrackingEvent rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
    }

    /**
     * Gets a list of package events.
     * @return A list of package events, sorted in ascending order.
     */
    public List<NZPostTrackingEvent> getEvents() {
        if (!areEventsSorted && events != null) {
            sortEventsByDate();
            areEventsSorted = true;
        }
        return events;
    }

    /**
     * Retrieves the most recent event, or null if there are no events. Also sets the package property for the returned event.
     * @return A {@link NZPostTrackingEvent} which can be compared to, or null if none is found.
     */
    public NZPostTrackingEvent getMostRecentEvent() {
        if (!areEventsSorted) {
            sortEventsByDate();
            areEventsSorted = true;
        }
        if (events != null && events.size() > 0) {
            NZPostTrackingEvent event = events.get(0);
            event.setParentPackage(code);
            return event;
        }
        return null;
    }

    public String getStatus() {
        return shortDescription;
    }

    public String getDetailedStatus() {
        return detailedDescription;
    }

    public boolean isTracked() {
        return !errorCode.equals("N");
    }

    public boolean hasPendingEvents() {
        return hasPendingEvents;
    }

    public void setHasPendingEvents(boolean hasPendingEvents) {
        this.hasPendingEvents = hasPendingEvents;
    }

    public String getTrackingCode() {
        return code;
    }

    public void setTrackingCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return label == null ? code : label;
    }

    public String getSource() {
        return source;
    }

    /**
     * Gets a printable form of the source property
     * @return The source as a human-readable title, or null if none exists.
     */
//    public String getSourceTitle() {
//        if (source == null) return "";
//        switch (source) {
//            case "nz_post":
//                return "NZ Post";
//
//            case "courier_post":
//                return "CourierPost";
//
//            default:
//                return "";
//        }
//    }


    public String getErrorCode() {
        return errorCode;
    }
    @Override
    public boolean equals(Object o) {
        NZPostTrackedPackage newPackage = o instanceof NZPostTrackedPackage ? (NZPostTrackedPackage) o : null;
        return newPackage != null && newPackage.getTrackingCode().equals(code);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public void setEvents(List<NZPostTrackingEvent> events) {
        this.events = events;
    }
}