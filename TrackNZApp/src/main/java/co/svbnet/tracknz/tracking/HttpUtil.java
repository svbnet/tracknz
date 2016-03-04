package co.svbnet.tracknz.tracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import co.svbnet.tracknz.BuildConfig;

public class HttpUtil {

    /**
     * The HTTP user agent header value to send when making an API request.
     */
    public static final String USER_AGENT = "TrackNZ/" + BuildConfig.VERSION_NAME;

    public static String downloadString(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
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
        return jsonString;
    }

}
