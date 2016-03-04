package co.svbnet.tracknz.util;

import java.util.regex.Pattern;

/**
 * Provides utilities for checking if a standard tracking code is valid.
 */
public class CodeValidationUtil {

    /**
     * The exact length of a valid code.
     */
    public static final int LENGTH = 13;
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}$");

    /**
     * Checks if a supplied tracking code is valid or not.
     * @param code The tracking code to test.
     * @return True if the code is valid, false if it is not.
     */
    public static boolean isValidCode(String code) {
        return !code.isEmpty()
                && code.length() == LENGTH
                && PATTERN.matcher(code.trim()).matches();
    }

}
