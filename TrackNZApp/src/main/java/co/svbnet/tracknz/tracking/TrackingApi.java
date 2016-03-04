package co.svbnet.tracknz.tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.svbnet.tracknz.BuildConfig;

/**
 * Provides a method for retrieving the details of NZ Post tracked packages.
 * Use NZPostTrackingService now.
 */
@Deprecated
public class TrackingApi {

    public static final int MAX_PACKAGES_PER_REQUEST = 10;
    /**
     * The API key for accessing the NZ Post tracking service.
     */
    public static final String NZP_TRACKING_API_KEY = "8ad4cbf0-47cb-0130-b979-005056920ffa";
    /**
     * The HTTP user agent header value to send when making an API request.
     */
    public static final String UA = "TrackNZ/" + BuildConfig.VERSION_NAME;
    /**
     * The Track API endpoint URL to use.
     */
    // Make sure debug URL is inlined
    public static final String ENDPOINT = BuildConfig.DEBUG ? "http://labs.svbnet.co/testdata/get_packages.php" : "https://api.nzpost.co.nz/tracking/track";
    /**
     * The URL format when sharing a NZ Post URL.
     */
    public static final String NZP_URL_FORMAT = "https://www.nzpost.co.nz/tools/tracking/item/%s";
    /**
     * The URL format to use when sharing a CourierPost URL.
     */
    public static final String CP_URL_FORMAT = "http://trackandtrace.courierpost.co.nz/search/%s";

    private String apiKey;

    public TrackingApi() {
        this.apiKey = NZP_TRACKING_API_KEY;
    }

    private URL createUrl(List<String> codes, boolean includeSignatureData) throws MalformedURLException {
        StringBuilder sb = new StringBuilder(ENDPOINT);
        sb.append("?license_key=%s&format=json&include_signature_data=%s");
        for (String code : codes) {
            sb.append("&tracking_code=");
            sb.append(code);
        }
        return new URL(String.format(sb.toString(), apiKey, includeSignatureData));
    }

    public ArrayList<TrackedPackage> getTrackedPackages(List<TrackedPackage> trackedPackages, boolean includeSignatureData) throws Exception {
        List<String> codes = new ArrayList<>();
        for (TrackedPackage trackedPackage : trackedPackages) {
            codes.add(trackedPackage.getCode());
        }
        return getTrackedPackagesByCode(codes, includeSignatureData);
    }

    public ArrayList<TrackedPackage> getTrackedPackagesByCode(List<String> codes, boolean includeSignatureData) throws IOException {
        // See https://www.nzpost.co.nz/business/developer-centre/tracking-api/track-method for docs

        // Build and send request
        URL trackingUrl = createUrl(codes, includeSignatureData);

        // Read response
        URLConnection connection = trackingUrl.openConnection();
        connection.setRequestProperty("User-Agent", UA);
        String line, jsonString = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        // Try-with-resources can only be used with API level > 19
        try {
            while ((line = reader.readLine()) != null) {
                jsonString += line;
            }
        } finally {
            reader.close();
        }

        Gson gson = new GsonBuilder().create();
        HashMap<String, TrackedPackage> packages = gson.fromJson(jsonString, new TypeToken<HashMap<String, TrackedPackage>>(){}.getType());

        // Turn packages into list instead of dictionary returned from API call
        ArrayList<TrackedPackage> packageList = new ArrayList<>();
        for (Map.Entry<String, TrackedPackage> package_ : packages.entrySet()) {
            TrackedPackage newPackage = package_.getValue();
            newPackage.setCode(package_.getKey());
            packageList.add(newPackage);
        }
        return packageList;
    }

}
