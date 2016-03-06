package co.svbnet.tracknz.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.PackageEventsArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;


public class PackageInfoActivity extends ToolbarActivity {

    public static final String PACKAGE_PARCEL = "PACKAGE_PARCEL";
    private NZPostTrackedPackage trackedPackage;
    private TrackingDB db = new TrackingDB(this);

    private TextView detailedDescription;

    private PackageEventsArrayAdapter eventsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_package_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trackedPackage = getIntent().getParcelableExtra(PACKAGE_PARCEL);
        if (trackedPackage.hasPendingEvents()) {
            db.clearPendingEvents(trackedPackage.getTrackingCode());
        }
        setupUi();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void setupUi() {
        detailedDescription = (TextView)findViewById(R.id.detailed_description);
        ListView eventsListView = (ListView) findViewById(R.id.events_list);
        eventsArrayAdapter = new PackageEventsArrayAdapter(this, trackedPackage.getEvents());
        eventsListView.setAdapter(eventsArrayAdapter);
        refreshUI();
    }

    private void refreshUI() {
        setTitle(trackedPackage.getTitle());
        detailedDescription.setText(trackedPackage.getDetailedStatus());
        if (trackedPackage.getLabel() != null) {
            getSupportActionBar().setSubtitle(trackedPackage.getTrackingCode());
        }
        if (trackedPackage.getSource() != null) {
            TextView srcTV = ((TextView) findViewById(R.id.source));
            switch (trackedPackage.getSource().toLowerCase()) {
                case "nz_post":
                    srcTV.setText("NZ Post");
                    break;
                case "courierpost":
                    srcTV.setText("CourierPost");
                    break;
                default:
                    srcTV.setText(trackedPackage.getSource());
                    break;
            }
        }
        eventsArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_package_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_code:
                ShareUtil.shareCode(this, trackedPackage.getTrackingCode());
                break;

            case R.id.action_share_url:
                ShareUtil.sharePackageUrl(this, trackedPackage);
                break;

            case R.id.action_set_label:
                PackageModifyUtil.editLabel(this, db, trackedPackage, new PackageModifyUtil.LabelEditComplete() {
                    @Override
                    public void onLabelEditComplete(String newLabel) {
                        if (newLabel == null) {
                            setTitle(trackedPackage.getTrackingCode());
                        } else {
                            setTitle(newLabel);
                            getSupportActionBar().setSubtitle(trackedPackage.getTrackingCode());
                        }
                    }
                });
                break;

            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_delete_packages)
                        .setMessage(
                                trackedPackage.getLabel() == null ?
                                        getString(R.string.message_delete_package, trackedPackage.getTrackingCode()) :
                                        getString(R.string.message_delete_package_with_label, trackedPackage.getLabel(), trackedPackage.getTrackingCode())
                        )
                        .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                db.deletePackage(trackedPackage.getTrackingCode());
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;

            case R.id.action_copy_code:
                ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, trackedPackage.getTrackingCode());
                clipboard.setPrimaryClip(clip);
                Toast toast = Toast.makeText(this, R.string.toast_copied_successfully, Toast.LENGTH_SHORT);
                toast.show();
                break;

            case R.id.action_open_in_browser:
                String url = "";
                if (trackedPackage.getSource().equals("nz_post")) {
                    url = NZPostTrackingService.getNZPostUrl(trackedPackage.getTrackingCode());
                } else if(trackedPackage.getSource().equals("courier_post")) {
                    url = NZPostTrackingService.getCourierPostUrl(trackedPackage.getTrackingCode());
                }
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;


        }
        return super.onOptionsItemSelected(item);
    }
}
