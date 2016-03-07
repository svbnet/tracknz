package co.svbnet.tracknz.util;

/**
 * Provides utilities for checking if a standard tracking code is valid.
 */
public class CodeValidationUtil {

    /**
     * Checks if a supplied tracking code is valid or not.
     * @param code The tracking code to test.
     * @return True if the code is valid, false if it is not.
     */
    public static boolean isValidCode(String code) {
        return !code.isEmpty()
                && !code.contains(" ");
    }

}
