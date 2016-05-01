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

import butterknife.Bind;
import butterknife.ButterKnife;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.fragment.PackageInfoFragment;
import co.svbnet.tracknz.fragment.PackageListFragment;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.CodeValidationUtil;


public class MainActivity extends ToolbarActivity implements PackageListFragment.OnPackageListInteraction {
    // Constants - Log tag
    private static final String TAG = "MainActivity";

    public static final int REQUEST_TRACKING_CODES = 1;
    public static final int REQUEST_BARCODE = 2;

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

        // Tablet in portrait mode and with a selected package
        // fragmentContainer is left-hand container on MDV, main container in single view
        isShowingFullPackage = isTablet && !isLandscape && selectedPackage != null;
        applyContainerVisibility();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedPackage = savedInstanceState.getParcelable("currentPackage");
        if (selectedPackage != null) {
            showPackageInInfoFragment(selectedPackage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("currentPackage", selectedPackage);
        super.onSaveInstanceState(outState);
    }

//    private void showListInMain() {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        setTitle(R.string.title_activity_main);
//        mListFragment = PackageListFragment.newInstance();
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
//                .replace(R.id.fragment_container, mListFragment)
//                .commit();
//        selectedPackage = null;
//    }
//
//    private void showFullPackage() {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        setTitle(R.string.title_activity_package_info);
//        mInfoFragment = PackageInfoFragment.newInstance(selectedPackage);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
//                .replace(R.id.fragment_container, mInfoFragment)
//                .commit();
//        isShowingFullPackage = true;
//    }

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
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mInfoFragment)
                .commit();
        isShowingFullPackage = false;
        applyContainerVisibility();
        applyActionBar();
    }

    @Override
    public void onItemClicked(NZPostTrackedPackage trackedPackage) {
        selectedPackage = trackedPackage;
        showPackageInInfoFragment(selectedPackage);
//        if (displayTwoPanes) {
//            showPackageInInfoFragment(trackedPackage);
//        } else {

//            showFullPackage();
//        }
    }

    private void showPackageInInfoFragment(NZPostTrackedPackage trackedPackage) {
        mInfoFragment = PackageInfoFragment.newInstance(trackedPackage);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                .replace(R.id.info_fragment_container, mInfoFragment)
                .commit();
        applyContainerVisibility();
        applyActionBar();
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
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
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

//    private class PackagesMultiChoiceListener implements AbsListView.MultiChoiceModeListener {
//
//        private List<Integer> getIndicesOfCheckedItems(SparseBooleanArray items) {
//            List<Integer> indices = new ArrayList<>();
//            for (int i = 0; i < items.size(); i++) {
//                if (items.valueAt(i)) {
//                    indices.add(items.keyAt(i));
//                }
//            }
//            return indices;
//        }
//
//        @Override
//        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//            int checkedItems = listView.getCheckedItemCount();
//            // If no items are selected, don't update to avoid awkward fade-out transition
//            if (checkedItems >= 1) {
//                mode.setTitle(getString(R.string.actionmode_selected, checkedItems));
//                // Apply visibility settings to singular menu items
//            }
//        }
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate(R.menu.menu_packages_contextual, menu);
//            // Hide FAB when in CAB mode
//            addFloatingButton.setVisibility(View.GONE);
//            // Stop refreshing
//            if (refreshTask != null) {
//                refreshTask.cancel(true);
//                swipeRefreshLayout.setRefreshing(false);
//
//            }
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }
//
//        @Override
//        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.action_set_label:
//                    // "There can only be one" - Gwen Stefani, from Hollaback Girl
//                    int itemId = getIndicesOfCheckedItems(listView.getCheckedItemPositions()).get(0);
//                    final NZPostTrackedPackage trackedPackage = adapterItems.get(itemId);
//                    PackageModifyUtil.editLabel(MainActivity.this, db, trackedPackage, new PackageModifyUtil.LabelEditComplete() {
//                        @Override
//                        public void onLabelEditComplete(String newLabel) {
//                            mode.finish();
//                            updateItems();
//                        }
//                    });
//                    break;
//
//                case R.id.action_share:
//                    List<Integer> shareIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
//                    String codes = "";
//                    for (Integer id : shareIds) {
//                            codes += adapterItems.get(id).getTrackingCode() + "\n";
//                    }
//                    ShareUtil.shareCode(MainActivity.this, codes);
//                    break;
//
//                case R.id.action_delete:
//                    final List<Integer> delItemIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
//                    final List<NZPostTrackedPackage> packagesToDelete = new ArrayList<>();
//                    StringBuilder sb = new StringBuilder();
//                    for (Integer id : delItemIds) {
//                        NZPostTrackedPackage selectedItem = adapterItems.get(id);
//                        packagesToDelete.add(selectedItem);
//                        sb.append("<b>");
//                        if (selectedItem.getLabel() == null) {
//                            sb.append(selectedItem.getTrackingCode());
//                            sb.append("</b>");
//                        } else {
//                            sb.append(selectedItem.getLabel());
//                            sb.append("</b>");
//                            sb.append(" (");
//                            sb.append(selectedItem.getTrackingCode());
//                            sb.append(")");
//                        }
//                        sb.append("<br>");
//                    }
//                    sb.append("<br>");
//                    int itemsSize = delItemIds.size();
//                    String msg;
//                    if (itemsSize == 1) {
//                        msg = MainActivity.this.getString(R.string.message_delete_package, sb.toString()).replace("<br>", "");
//                    } else {
//                        msg = MainActivity.this.getString(R.string.message_delete_packages, sb.toString());
//                    }
//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle(R.string.title_delete_packages)
//                            .setMessage(Html.fromHtml(msg))
//                            .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    for (NZPostTrackedPackage id : packagesToDelete) {
//                                            db.deletePackage(id.getTrackingCode());
//                                    }
//                                    updateItems();
//                                    dialog.dismiss();
//                                    mode.finish();
//                                }
//                            })
//                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                    break;
//            }
//            return true;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            addFloatingButton.setVisibility(View.VISIBLE);
//        }
//    }


}
