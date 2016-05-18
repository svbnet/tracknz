package co.svbnet.tracknz.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.tracking.PackageFlag;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;

/**
 * ArrayAdapter for package tracking events.
 */
public class PackageEventsArrayAdapter extends ArrayAdapter<NZPostTrackingEvent> {

    private Context context;
    private List<NZPostTrackingEvent> events;

    public PackageEventsArrayAdapter(Context context, List<NZPostTrackingEvent> events) {
        super(context, R.layout.item_package_event, events);
        this.context = context;
        this.events = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_package_event, parent, false);
        }
        NZPostTrackingEvent event = events.get(position);
        ImageView statusIcon = (ImageView)rowView.findViewById(R.id.status_icon);
        TextView descriptionText = (TextView)rowView.findViewById(R.id.description);
        descriptionText.setText(event.getDescription());
        TextView dateText = (TextView)rowView.findViewById(R.id.date);
        dateText.setText(DateUtils.getRelativeDateTimeString(context, event.getDate().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
        statusIcon.setBackgroundResource(PackageFlag.getBackgroundDrawableForFlag(event.getFlag()));
        statusIcon.setImageResource(PackageFlag.getImageDrawableForFlag(event.getFlag()));
        return rowView;
    }
}
