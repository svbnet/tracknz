package co.svbnet.tracknz.tracking.fastway;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Joe on 6/02/2016.
 */
public class FastwayPackageEvent {

    @SerializedName("Type")
    private String flag;

    @SerializedName("Description")
    private String shortDescription;

    @SerializedName("StatusDescription")
    private String description;

    @SerializedName("Date")
    private String datetime;
}
