package co.svbnet.tracknz.tracking.fastway;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import co.svbnet.tracknz.tracking.HttpUtil;
import co.svbnet.tracknz.tracking.TrackedPackage;
import co.svbnet.tracknz.tracking.TrackingService;

/**
 * Created by Joe on 6/02/2016.
 */
public class FastwayTrackingService implements TrackingService {

    private class FastwayResponse {
        
    }

    public static final String NAME = "Fastway";
    public static final String SOURCE = "fastway";

    private static final String API_KEY = "2b5d04bcfb2ca813cdd45f06d414220e";
    private static final String ENDPOINT = "https://api.fastway.org/v4/tracktrace/massdetail/%s?api_key=%s";
    static final String URL_FORMAT = "http://www.fastway.co.nz/track/track-your-parcel?l=%s";

    private URL createUrl(List<String> codes) throws MalformedURLException {
        // join() the codes list by a semicolon
        StringBuilder codeStringSb = new StringBuilder();
        boolean first = true;
        for (String code : codes) {
            if (!first)
                codeStringSb.append(";");
            else
                first = false;
            codeStringSb.append(code);
        }
        String codeString = codeStringSb.toString();

        return new URL(String.format(ENDPOINT, codeString, API_KEY));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TrackedPackage> retrievePackages(List<String> codes) throws IOException {
        String json = HttpUtil.downloadString(createUrl(codes));
        Gson gson = new GsonBuilder().create();
        // This returns an object with the properties result and generated_in; we only want result, which is
        // an array of code objects
        HashMap responseObject = gson.fromJson(json, HashMap.class);
        // We can also get an error, so we need to check for that
        if (responseObject.containsKey("error") || !responseObject.containsKey("result")) {
            return Collections.EMPTY_LIST;
        }
        return (List<TrackedPackage>)responseObject.get("result");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
