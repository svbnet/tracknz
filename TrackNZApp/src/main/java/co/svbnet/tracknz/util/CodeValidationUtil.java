package co.svbnet.tracknz.util;

import java.util.regex.Pattern;

/**
 * Provides utilities for checking if a standard tracking code is valid.
 */
public class CodeValidationUtil {

    private static final String VALIDATION_EXPRESSION = "^[a-zA-Z0-9_-]*$";

    public static final Pattern PATTERN = Pattern.compile(VALIDATION_EXPRESSION);

    /**
     * Checks if a supplied tracking code is valid or not.
     * @param code The tracking code to test.
     * @return True if the code is valid, false if it is not.
     */
    public static boolean isValidCode(String code) {
        return code != null && !code.isEmpty() && PATTERN.matcher(code).matches();
    }

}
