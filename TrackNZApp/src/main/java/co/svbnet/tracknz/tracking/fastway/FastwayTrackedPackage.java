package co.svbnet.tracknz.tracking.fastway;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import co.svbnet.tracknz.tracking.TrackedPackage;

/**
 * Created by Joe on 6/02/2016.
 */
public class FastwayTrackedPackage extends TrackedPackage {

    @SerializedName("LabelNo")
    private String code;

    @SerializedName("error")
    private String error;

    @SerializedName("Scans")
    private List<FastwayPackageEvent> events;

}
