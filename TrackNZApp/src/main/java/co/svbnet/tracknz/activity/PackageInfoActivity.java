package co.svbnet.tracknz.activity;

import android.os.Bundle;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.fragment.PackageInfoFragment;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.ui.ToolbarActivity;

@Deprecated
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
