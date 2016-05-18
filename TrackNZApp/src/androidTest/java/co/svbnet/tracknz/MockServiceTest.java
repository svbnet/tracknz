package co.svbnet.tracknz;

import com.google.gson.Gson;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import co.svbnet.tracknz.tracking.HttpUtil;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;

/**
 * Created by Joe on 10/05/2016.
 */
public class MockServiceTest extends TestCase {

    protected List<String> codes = new ArrayList<>();
    protected NZPostTrackingService service = new NZPostTrackingService();

    private static final String ENDPOINT = "http://mercury:5000/";

    @Override
    protected void setUp() throws Exception {
        assertTrue("Must be using debug URL", BuildConfig.DEBUG);
        String packageCodesJson = HttpUtil.downloadString(new URL(ENDPOINT + "codes"));
        codes = new Gson().fromJson(packageCodesJson, ArrayList.class);
    }

    public void testDoesTruthExist() {
        assertTrue(true);
    }

    public void testRequestAndResponseParsing() throws IOException {
        List<NZPostTrackedPackage> packages = service.retrievePackages(codes);

    }
}
