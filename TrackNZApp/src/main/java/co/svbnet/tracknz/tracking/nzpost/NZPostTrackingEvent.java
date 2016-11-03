package co.svbnet.tracknz.tracking.nzpost;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.Date;

import co.svbnet.tracknz.tracking.PackageFlag;

/**
 * Represents an event which has occurred over the lifetime of a tracked package.
 */
public class NZPostTrackingEvent implements Parcelable {

    private String parentCode;
    @SerializedName("flag")
    private String flagString;
    private int flagInt = PackageFlag.FLAG_UNKNOWN;
    @SerializedName("description")
    private String description;
    @SerializedName("datetime")
    private String dateString;
    // NZ Post returns a string in the object named "date", which is a date string which confuses gson
    private Date dateObj;

    public NZPostTrackingEvent() {
    }

    protected NZPostTrackingEvent(Parcel in) {
        parentCode = in.readString();
        flagInt = in.readInt();
        description = in.readString();
        dateString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parentCode);
        dest.writeInt(flagInt);
        dest.writeString(description);
        if (dateString == null) {
            dateString = DateFormatUtil.FORMAT.format(dateObj);
        }
        dest.writeString(dateString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NZPostTrackingEvent> CREATOR = new Creator<NZPostTrackingEvent>() {
        @Override
        public NZPostTrackingEvent createFromParcel(Parcel in) {
            return new NZPostTrackingEvent(in);
        }

        @Override
        public NZPostTrackingEvent[] newArray(int size) {
            return new NZPostTrackingEvent[size];
        }
    };

    public String getParentPackage() {
        return parentCode;
    }

    public void setParentPackage(String parentCode) {
        this.parentCode = parentCode;
    }

    public int getFlag() {
        if (flagInt == PackageFlag.FLAG_UNKNOWN && flagString != null && !flagString.isEmpty()) {
            flagInt = flagString.charAt(0);
            flagString = null;
        }
        return flagInt;
    }

    public void setFlag(int flag) {
        this.flagInt = flag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private boolean parseDate() {
        if (dateObj == null) {
            try {
                dateObj = DateFormatUtil.parseNZPDate(dateString);
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    public Date getDate() {
        if (parseDate()) {
            return dateObj;
        }
        return new Date();
    }

    public void setDate(Date date) {
        this.dateObj = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NZPostTrackingEvent that = (NZPostTrackingEvent) o;

        if (flagInt != that.flagInt) return false;
        if (parentCode != null ? !parentCode.equals(that.parentCode) : that.parentCode != null)
            return false;
        return dateObj != null ? dateObj.equals(that.dateObj) : that.dateObj == null;

    }

    @Override
    public int hashCode() {
        int result = parentCode != null ? parentCode.hashCode() : 0;
        result = 31 * result + flagInt;
        result = 31 * result + (dateObj != null ? dateObj.hashCode() : 0);
        return result;
    }
}