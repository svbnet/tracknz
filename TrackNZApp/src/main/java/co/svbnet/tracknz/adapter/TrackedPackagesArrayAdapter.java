package co.svbnet.tracknz.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;

/**
 * ArrayAdapter for tracked packages.
 */
public class TrackedPackagesArrayAdapter extends ArrayAdapter<NZPostTrackedPackage> {

    private final Context context;
    private final List<NZPostTrackedPackage> items;

    public TrackedPackagesArrayAdapter(Context context, List<NZPostTrackedPackage> items) {
        super(context, R.layout.item_tracked_package, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NZPostTrackedPackage item = items.get(position);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_tracked_package, parent, false);
        }

        ImageView statusIcon = (ImageView)rowView.findViewById(R.id.status_icon);
        TextView titleView = (TextView)rowView.findViewById(R.id.title);
        TextView shortDescView = (TextView)rowView.findViewById(R.id.short_description);
        TextView dateView = (TextView)rowView.findViewById(R.id.last_event_date);

        titleView.setText(item.getTitle());

        if (item.isTracked()) {
            NZPostTrackingEvent latestEvent = item.getMostRecentEvent();
            statusIcon.setBackgroundResource(PackageFlag.getBackgroundDrawableForFlag(latestEvent.getFlag()));
            statusIcon.setImageResource(PackageFlag.getImageDrawableForFlag(latestEvent.getFlag()));
            shortDescView.setText(item.getMostRecentEvent().getDescription());
            dateView.setText(DateUtils.getRelativeTimeSpanString(context, latestEvent.getDate().getTime()));
        } else {
            statusIcon.setBackgroundResource(R.drawable.tracking_status_icon_not_entered);
            statusIcon.setImageResource(R.drawable.ic_not_found);
            shortDescView.setText(R.string.package_doesnt_exist);
        }

        // If we have pending events, then set to bold
        if (item.hasPendingEvents()) {
            titleView.setTypeface(null, Typeface.BOLD);
            shortDescView.setTypeface(null, Typeface.BOLD);
        } else {
            titleView.setTypeface(null, Typeface.NORMAL);
            shortDescView.setTypeface(null, Typeface.NORMAL);
        }

        return rowView;
    }
}
