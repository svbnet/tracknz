package co.svbnet.tracknz.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.fragment.PackageInfoFragment;
import co.svbnet.tracknz.fragment.PackageListFragment;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.CodeValidationUtil;

/**
 * The main activity of the application: This is the launcher activity, and hosts the package info
 * and package list fragments.
 */
public class MainActivity extends ToolbarActivity
        implements PackageListFragment.OnPackageListInteraction,
        PackageInfoFragment.InfoFragmentCallbacks {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_TRACKING_CODES = 1;

    /**
     * Extras bundle key for the package to be displayed in the info fragment.
     */
    public static final String CURRENT_PACKAGE = "current_package";
    private static final String CURRENT_PACKAGE_SAVED_INSTANCE = "current_package_saved_instance";

    @Nullable @Bind(R.id.info_fragment_container) FrameLayout infoFragmentContainer;
    @Bind(R.id.fragment_container) FrameLayout mFragmentContainer;
    //@Nullable @Bind(R.id.unselected_view) View unselectedView;

    private boolean displayTwoPanes;
    private boolean isTablet;
    private boolean isLandscape;
    private boolean isShowingFullPackage;

    private NZPostTrackedPackage selectedPackage;

    PackageListFragment mListFragment;
    PackageInfoFragment mInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTablet = getResources().getBoolean(R.bool.is_tablet);
        isLandscape = getResources().getBoolean(R.bool.is_landscape);
        displayTwoPanes = isTablet && isLandscape;
        setContentViewAndToolbar(R.layout.activity_main_master_detail);
        ButterKnife.bind(this);
        applyActionBar();
        mListFragment = PackageListFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mListFragment)
                .commit();
        selectedPackage = getIntent().getParcelableExtra(CURRENT_PACKAGE);

        // Tablet in portrait mode and with a selected package
        // fragmentContainer is left-hand container on MDV, main container in single view
        isShowingFullPackage = isTablet && !isLandscape && selectedPackage != null;
        applyContainerVisibility();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedPackage = savedInstanceState.getParcelable(CURRENT_PACKAGE_SAVED_INSTANCE);
        if (selectedPackage != null) {
            showPackageInInfoFragment(selectedPackage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CURRENT_PACKAGE_SAVED_INSTANCE, selectedPackage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isShowingFullPackage) {
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void goBack() {
        selectedPackage = null;
        applyContainerVisibility();
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mInfoFragment)
                .commit();
        isShowingFullPackage = false;
        applyActionBar();
    }

    @Override
    public void onItemSelected(NZPostTrackedPackage trackedPackage) {
        selectedPackage = trackedPackage;
        showPackageInInfoFragment(selectedPackage);
    }

    @Override
    public void onSelectedItemChanged() {
        if (isShowingFullPackage) {
            selectedPackage = mListFragment.getSelectedPackage();
            showPackageInInfoFragment(selectedPackage);
        }
    }

    @Override
    public void onSelectedItemDeleted() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mInfoFragment)
                .commitAllowingStateLoss();
        mInfoFragment = null;
        selectedPackage = null;
    }

    private void showPackageInInfoFragment(NZPostTrackedPackage trackedPackage) {
        selectedPackage = trackedPackage;
        mInfoFragment = PackageInfoFragment.newInstance(trackedPackage);

        applyContainerVisibility();
        applyActionBar();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                .replace(R.id.info_fragment_container, mInfoFragment)
                // Fix for java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState bug on support library
                .commitAllowingStateLoss();
    }

    private void applyActionBar() {
        if (isShowingFullPackage) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(selectedPackage.getTitle());
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            setTitle(R.string.title_activity_main);
        }
    }

    private void applyContainerVisibility() {
        isShowingFullPackage = !displayTwoPanes && selectedPackage != null;
        if (displayTwoPanes) {
            mFragmentContainer.setVisibility(View.VISIBLE);
            infoFragmentContainer.setVisibility(View.VISIBLE);
        } else {
            if (selectedPackage == null) {
                mFragmentContainer.setVisibility(View.VISIBLE);
                infoFragmentContainer.setVisibility(View.GONE);
            } else {
                mFragmentContainer.setVisibility(View.GONE);
                infoFragmentContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void requestManualEntry(@Nullable String code) {
        Intent intent = new Intent(this, CodeInputActivity.class);
        if (code != null) {
            intent.putExtra(CodeInputActivity.CODE, code);
        }
        startActivityForResult(intent, REQUEST_TRACKING_CODES);
    }

    @Override
    public void requestBarcode() {
        requestScan();
    }

    private void requestScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isShowingFullPackage) {
                    goBack();
                }
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TRACKING_CODES) {
            if (resultCode == RESULT_OK) {
                selectedPackage = data.getParcelableExtra(CURRENT_PACKAGE);
                mListFragment.getItems().add(selectedPackage);
                mListFragment.invalidateItems();
                showPackageInInfoFragment(selectedPackage);
                int packageIndex = mListFragment.getItems().indexOf(selectedPackage);
                mListFragment.selectItem(packageIndex);
            }
            return;
        }
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult == null || scanResult.getContents() == null) {
            return;
        }
        String code = scanResult.getContents();
        if (!CodeValidationUtil.isValidCode(code)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_error)
                    .setMessage(Html.fromHtml(getString(R.string.error_code_scanned_invalid, code)))
                    .setPositiveButton(R.string.dialog_button_try_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestScan();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            Intent intent = new Intent(this, CodeInputActivity.class);
            intent.putExtra(CodeInputActivity.CODE, code);
            startActivityForResult(intent, REQUEST_TRACKING_CODES);
        }
    }

    @Override
    public void onChange(NZPostTrackedPackage trackedPackage) {
        List<NZPostTrackedPackage> trackedPackageList = mListFragment.getItems();
        int idx = trackedPackageList.indexOf(trackedPackage);
        trackedPackageList.set(idx, trackedPackage);
        mListFragment.invalidateItems();
    }

    @Override
    public void onRemove(NZPostTrackedPackage trackedPackage) {
        mListFragment.getItems().remove(trackedPackage);
        mListFragment.invalidateItems();
        selectedPackage = null;
        goBack();
    }

}
