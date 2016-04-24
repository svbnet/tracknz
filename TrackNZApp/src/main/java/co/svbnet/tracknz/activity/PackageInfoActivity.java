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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.PackageEventsArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.fragment.PackageInfoFragment;
import co.svbnet.tracknz.tracking.PackageFlag;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingEvent;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;


public class PackageInfoActivity extends ToolbarActivity {

    public static final String PACKAGE_PARCEL = "PACKAGE_PARCEL";
    private NZPostTrackedPackage trackedPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_package_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trackedPackage = getIntent().getParcelableExtra(PACKAGE_PARCEL);
        PackageInfoFragment fragment = PackageInfoFragment.newInstance(trackedPackage);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.info_fragment_container, fragment)
                .commit();
    }
}
