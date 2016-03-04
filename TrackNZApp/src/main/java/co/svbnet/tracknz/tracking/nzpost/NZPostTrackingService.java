package co.svbnet.tracknz.tracking.nzpost;

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
import co.svbnet.tracknz.tracking.HttpUtil;
import co.svbnet.tracknz.tracking.TrackedPackage;
import co.svbnet.tracknz.tracking.TrackingService;

/**
 * Created by Joe on 3/01/2016.
 */
public class NZPostTrackingService implements TrackingService {

    private static final String NZP_TRACKING_API_KEY = "8ad4cbf0-47cb-0130-b979-005056920ffa";
    private static final String ENDPOINT = BuildConfig.DEBUG ? "http://labs.svbnet.co/testdata/get_packages.php" : "https://api.nzpost.co.nz/tracking/track";
    private static final String NZP_URL_FORMAT = "https://www.nzpost.co.nz/tools/tracking/item/%s";
    public static final String NAME = "NZ Post";
    public static final String SOURCE = "nz_post";

    private URL createUrl(List<String> codes, boolean includeSignatureData) throws MalformedURLException {
        StringBuilder sb = new StringBuilder(ENDPOINT);
        sb.append("?license_key=%s&format=json&include_signature_data=%s");
        for (String code : codes) {
            sb.append("&tracking_code=");
            sb.append(code);
        }
        return new URL(String.format(sb.toString(), NZP_TRACKING_API_KEY, includeSignatureData));
    }

    @Override
    public List<TrackedPackage> retrievePackages(List<String> codes) throws IOException {
        // See https://www.nzpost.co.nz/business/developer-centre/tracking-api/track-method for docs

        // Build and send request
        URL trackingUrl = createUrl(codes, false);

        // Read response
        URLConnection connection = trackingUrl.openConnection();
        connection.setRequestProperty("User-Agent", HttpUtil.USER_AGENT);
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
        HashMap<String, NZPostTrackedPackage> packages = gson.fromJson(jsonString, new TypeToken<HashMap<String, NZPostTrackedPackage>>(){}.getType());

        // Turn packages into list instead of dictionary returned from API call
        List<TrackedPackage> packageList = new ArrayList<>();
        for (Map.Entry<String, NZPostTrackedPackage> package_ : packages.entrySet()) {
            NZPostTrackedPackage newPackage = package_.getValue();
            newPackage.setTrackingCode(package_.getKey());
            packageList.add(newPackage);
        }
        return packageList;
    }

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
        return String.format(NZP_URL_FORMAT, code);
    }

    @Override
    public boolean isCodeValid(String code) {
        return false;
    }
}
