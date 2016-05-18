package co.svbnet.tracknz.tracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import co.svbnet.tracknz.BuildConfig;

/**
 * Provides convenience methods for requesting HTTP data.
 */
public class HttpUtil {

    /**
     * The HTTP user agent header value to send when making an API request.
     */
    public static final String USER_AGENT = "TrackNZ/" + BuildConfig.VERSION_NAME;
    /**
     * The amount of time the HTTP client will wait for a connection.
     */
    public static final int TIMEOUT = 10000;

    /**
     * Downloads the string response of a URL GET request.
     * @param url The URL to request with.
     * @return The contents of the URL
     * @throws IOException
     */
    public static String downloadString(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(TIMEOUT);
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
