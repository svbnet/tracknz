package co.svbnet.tracknz.tracking.nzpost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtil {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

    static Date parseNZPDate(String dateFormat) throws ParseException {
        // Because the API only returns ISO 8601-style dates and there is no
        return DateFormatUtil.FORMAT.parse(dateFormat.replace("+12:00", "+1200"));
    }

}