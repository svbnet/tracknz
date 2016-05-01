package co.svbnet.tracknz.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.PackageEventsArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tracking.PackageFlag;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;

public class PackageInfoFragment extends Fragment {

    private static final String ARG_PACKAGE = "package";

    private NZPostTrackedPackage mPackage;
    private TrackingDB db;

    @Bind(R.id.detailed_description) TextView detailedDescription;
    @Bind(R.id.label) TextView labelLabel;
    @Bind(R.id.status_icon) ImageView statusIcon;
    @Bind(R.id.info_layout) RelativeLayout infoLayout;
    @Bind(R.id.events_list) ListView eventsListView;
    @Bind(R.id.empty_events) LinearLayout emptyEventsLayout;
    @Bind(R.id.source) TextView sourceText;

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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mPackage = getArguments().getParcelable(ARG_PACKAGE);
            if (mPackage.hasPendingEvents()) {
                db.clearPendingEvents(mPackage.getTrackingCode());
            }
        }
    }

    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_package_info, container, false);
        ButterKnife.bind(this, view);
        mEventsArrayAdapter = new PackageEventsArrayAdapter(this.getContext(), mPackage.getEvents());
        eventsListView.setAdapter(mEventsArrayAdapter);
        refreshUI();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Only inflate if the fragment is visible -- this prevents the menu from showing when going
        // landscape to portrait
        if (isVisible()) {
            inflater.inflate(R.menu.menu_package_info, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_code:
                ShareUtil.shareCode(getActivity(), mPackage.getTrackingCode());
                break;

            case R.id.action_share_url:
                ShareUtil.sharePackageUrl(getActivity(), mPackage);
                break;

            case R.id.action_set_label:
                PackageModifyUtil.editLabel(getContext(), db, mPackage, new PackageModifyUtil.LabelEditComplete() {
                    @Override
                    public void onLabelEditComplete(String newLabel) {
                        if (newLabel != null) {
                            labelLabel.setVisibility(View.VISIBLE);
                            labelLabel.setText(mPackage.getTrackingCode());
                        } else {
                            labelLabel.setVisibility(View.GONE);
                        }
                    }
                });
                break;

//            case R.id.action_delete:
//                new AlertDialog.Builder(getContext())
//                        .setTitle(R.string.title_delete_packages)
//                        .setMessage(
//                                trackedPackage.getLabel() == null ?
//                                        getString(R.string.message_delete_package, trackedPackage.getTrackingCode()) :
//                                        getString(R.string.message_delete_package_with_label, trackedPackage.getLabel(), trackedPackage.getTrackingCode())
//                        )
//                        .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                db.deletePackage(trackedPackage.getTrackingCode());
//                                finish();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//                break;

            case R.id.action_copy_code:
                ClipboardManager clipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, mPackage.getTrackingCode());
                clipboard.setPrimaryClip(clip);
                Toast toast = Toast.makeText(getContext(), R.string.toast_copied_successfully, Toast.LENGTH_SHORT);
                toast.show();
                break;

            case R.id.action_open_in_browser:
                String url = "";
                if (mPackage.getSource().equals("nz_post")) {
                    url = NZPostTrackingService.getNZPostUrl(mPackage.getTrackingCode());
                } else if(mPackage.getSource().equals("courier_post")) {
                    url = NZPostTrackingService.getCourierPostUrl(mPackage.getTrackingCode());
                }
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshUI() {
        if (mPackage.getLabel() != null) {
            labelLabel.setVisibility(View.VISIBLE);
            labelLabel.setText(mPackage.getTrackingCode());
        } else {
            labelLabel.setVisibility(View.GONE);
        }
        if (mPackage.isTracked()) {
            eventsListView.setVisibility(View.VISIBLE);
            emptyEventsLayout.setVisibility(View.GONE);
            detailedDescription.setText(mPackage.getDetailedStatus());
            NZPostTrackingEvent latestEvent = mPackage.getMostRecentEvent();
//            toolbar.setBackgroundResource(PackageFlag.getColorForFlag(latestEvent.getFlag()));
            infoLayout.setBackgroundResource(PackageFlag.getColorForFlag(latestEvent.getFlag()));
            statusIcon.setImageResource(PackageFlag.getImageDrawableForFlag(latestEvent.getFlag()));
            if (mPackage.getSource() != null) {
                switch (mPackage.getSource().toLowerCase()) {
                    case "nz_post":
                        sourceText.setText("NZ Post");
                        break;
                    case "courier_post":
                        sourceText.setText("CourierPost");
                        break;
                    default:
                        sourceText.setText(mPackage.getSource());
                        break;
                }
            }
            mEventsArrayAdapter.notifyDataSetChanged();
        } else {
            //toolbar.setBackgroundResource(R.color.tracking_status_not_entered);
            infoLayout.setBackgroundResource(R.color.tracking_status_not_entered);
            statusIcon.setImageResource(R.drawable.ic_not_found);
            detailedDescription.setText(R.string.package_doesnt_exist_toast);
            eventsListView.setVisibility(View.GONE);
            emptyEventsLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = new TrackingDB(context);
    }

}
