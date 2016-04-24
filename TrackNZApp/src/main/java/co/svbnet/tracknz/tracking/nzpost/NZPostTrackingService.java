package co.svbnet.tracknz.tracking.nzpost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.svbnet.tracknz.BuildConfig;
import co.svbnet.tracknz.tracking.HttpUtil;

/**
 * Implements the NZ Post track and trace service.
 */
public class NZPostTrackingService {

    private static final String NZP_TRACKING_API_KEY = "8ad4cbf0-47cb-0130-b979-005056920ffa";
    private static final String ENDPOINT = BuildConfig.DEBUG ? "http://mercury/mock_service.php" : "https://api.nzpost.co.nz/tracking/track";
    private static final String NZP_URL_FORMAT = "https://www.nzpost.co.nz/tools/tracking/item/%s";
    private static final String CP_URL_FORMAT = "https://trackandtrace.courierpost.co.nz/search/%s";

    private URL createUrl(List<String> codes) throws MalformedURLException {
        StringBuilder sb = new StringBuilder(ENDPOINT);
        sb.append("?license_key=%s&format=json");
        for (String code : codes) {
            sb.append("&tracking_code=");
            sb.append(code);
        }
        return new URL(String.format(sb.toString(), NZP_TRACKING_API_KEY));
    }

    public List<NZPostTrackedPackage> retrievePackages(List<String> codes) throws IOException {
        // See https://www.nzpost.co.nz/business/developer-centre/tracking-api/track-method for docs

        // Build and send request
        URL trackingUrl = createUrl(codes);
        String jsonString = HttpUtil.downloadString(trackingUrl);

        Gson gson = new GsonBuilder().create();
        HashMap<String, NZPostTrackedPackage> packages = gson.fromJson(jsonString, new TypeToken<HashMap<String, NZPostTrackedPackage>>(){}.getType());

        // Turn packages into list instead of dictionary returned from API call
        List<NZPostTrackedPackage> packageList = new ArrayList<>();
        for (Map.Entry<String, NZPostTrackedPackage> package_ : packages.entrySet()) {
            NZPostTrackedPackage newPackage = package_.getValue();
            newPackage.setTrackingCode(package_.getKey());
            packageList.add(newPackage);
        }
        return packageList;
    }

    public static String getNZPostUrl(String code) {
        return String.format(NZP_URL_FORMAT, code);
    }

    public static String getCourierPostUrl(String code) {
        return String.format(CP_URL_FORMAT, code);
    }
}
