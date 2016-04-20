package co.svbnet.tracknz.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.PackageEventsArrayAdapter;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;

public class PackageInfoFragment extends Fragment {

    private static final String ARG_PACKAGE = "package";

    private NZPostTrackedPackage mPackage;

    @Bind(R.id.detailed_description) TextView detailedDescription;
    @Bind(R.id.label) TextView labelLabel;
    @Bind(R.id.status_icon) ImageView statusIcon;
    @Bind(R.id.info_layout) RelativeLayout infoLayout;
    @Bind(R.id.events_list) ListView eventsListView;
    @Bind(R.id.empty_events) LinearLayout emptyEventsLayout;

    private PackageEventsArrayAdapter mEventsArrayAdapter;

    public PackageInfoFragment() {
        // Required empty public constructor
    }

    public static PackageInfoFragment newInstance(NZPostTrackedPackage trackedPackage) {
        PackageInfoFragment fragment = new PackageInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PACKAGE, trackedPackage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPackage = getArguments().getParcelable(ARG_PACKAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_package_info, container, false);
        ButterKnife.bind(this, view);
        mEventsArrayAdapter = new PackageEventsArrayAdapter(this.getContext(), mPackage.getEvents());
        eventsListView.setAdapter(mEventsArrayAdapter);
        return view;
    }
}
