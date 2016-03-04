package co.svbnet.tracknz.tracking.nzpost;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.Date;

import co.svbnet.tracknz.tracking.TrackingEvent;
import co.svbnet.tracknz.tracking.TrackingFlag;

/**
 * Represents an event which has occurred over the lifetime of a tracked package.
 */
public class NZPostTrackingEvent implements TrackingEvent, Parcelable {


    private String parentCode;
    @SerializedName("flag")
    private String flagString;
    private int flagInt = TrackingFlag.FLAG_UNKNOWN;
    @SerializedName("description")
    private String description;
    @SerializedName("datetime")
    private String dateTime;

    public NZPostTrackingEvent() {
    }

    @Override
    public String getParentPackage() {
        return parentCode;
    }

    void setParentPackage(String parentCode) {
        this.parentCode = parentCode;
    }

    @Override
    public int getFlag() {
        if (flagInt == TrackingFlag.FLAG_UNKNOWN && flagString != null && !flagString.isEmpty()) {
            flagInt = flagString.charAt(0);
            flagString = null;
        }
        return flagInt;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getDate() {
        try {
            return DateFormatUtil.parseNZPDate(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        NZPostTrackingEvent event = o instanceof NZPostTrackingEvent ? (NZPostTrackingEvent) o : null;
        return event != null
                && event.getFlag() == getFlag()
                && event.getDate().equals(getDate())
                && event.parentCode.equals(parentCode);
    }

    protected NZPostTrackingEvent(Parcel in) {
        parentCode = in.readString();
        flagString = in.readString();
        flagInt = in.readInt();
        description = in.readString();
        dateTime = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parentCode);
        dest.writeString(flagString);
        dest.writeInt(flagInt);
        dest.writeString(description);
        dest.writeString(dateTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NZPostTrackingEvent> CREATOR = new Parcelable.Creator<NZPostTrackingEvent>() {
        @Override
        public NZPostTrackingEvent createFromParcel(Parcel in) {
            return new NZPostTrackingEvent(in);
        }

        @Override
        public NZPostTrackingEvent[] newArray(int size) {
            return new NZPostTrackingEvent[size];
        }
    };
}