package co.svbnet.tracknz;

import junit.framework.TestCase;

import co.svbnet.tracknz.util.CodeValidationUtil;

/**
 * Created by Joe on 10/05/2016.
 */
public class CodeValidityTest extends TestCase {

    public void testValidPackageTrackingCodes() {
        String[] codes = {
                "RF123456789NZ",
                "AA000111222BB",
                "01800172",
                "EF4123239837734334",
                "1005410001051701ASH003JS",
                "beppo",
                "foODYeah",
                "99999999999999999999999999999999999999999999"
        };
        for (String code : codes) {
            assertTrue(code, CodeValidationUtil.isValidCode(code));
        }
    }

    public void testInvalidPackageTrackingCodes() {
        String[] codes = {
                "str+okjghjnn",
                "Pizza Monkey Bread",
                "http://www.google.com",
                "RF\n123456789NZ",
                "B\tR8976789212NZ",
                "0890928773\r\n",
                "CRCR93890298978\n",
                "NN 123434224ZZ",
                "\nPPPEPEPEPEPE",
                "PEA\u2343N\u800bIS",
                "                    "
        };
        for (String code : codes) {
            assertFalse(code, CodeValidationUtil.isValidCode(code));
        }
    }

}
