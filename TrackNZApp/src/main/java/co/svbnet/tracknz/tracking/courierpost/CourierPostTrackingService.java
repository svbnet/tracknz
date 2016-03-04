package co.svbnet.tracknz.tracking.courierpost;

import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;

/**
 * Created by Joe on 18/02/2016.
 */
public class CourierPostTrackingService extends NZPostTrackingService {
    public static final String NAME = "CourierPost";
    public static final String SOURCE = "courierpost";
    private static final String URL_FORMAT = "http://trackandtrace.courierpost.co.nz/search/%s";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public String getUrlForCode(String code) {
        return String.format(URL_FORMAT, code);
    }
}
