package co.svbnet.tracknz.tracking;

import java.io.IOException;
import java.util.List;

/**
 * Represents a generic package tracking service.
 */
public interface TrackingService {
    /**
     * Retrieves a list of packages from the tracking service's server.
     * @param codes A list of valid package codes
     * @return A complete list of packages
     * @throws IOException If the HTTP request fails
     */
    List<TrackedPackage> retrievePackages(List<String> codes) throws IOException;

    /**
     * Gets a human-readable name for the service.
     * @return A human-readable name
     */
    String getName();

    /**
     * Gets a unique service identifier for the service.
     * @return A source string
     */
    String getSource();

    /**
     * Formats a tracking code into a publicly-accessible URL that links to the service website's
     * tracking webpage.
     * @param code The code to format.
     * @return A URL string, or null if none is available.
     */
    String getUrlForCode(String code);

    /**
     * Checks if the code is valid.
     * @param code A package code to be tested.
     * @return Returns true if the code is valid or if there is no validation logic required, false
     * if the code is invalid.
     */
    boolean isCodeValid(String code);
}
