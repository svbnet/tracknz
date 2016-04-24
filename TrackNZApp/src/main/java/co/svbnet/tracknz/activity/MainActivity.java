package co.svbnet.tracknz.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.TrackedPackagesArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.fragment.PackageInfoFragment;
import co.svbnet.tracknz.fragment.PackageListFragment;
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.BarcodeScannerUtil;
import co.svbnet.tracknz.util.CodeValidationUtil;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;


public class MainActivity extends ToolbarActivity implements PackageListFragment.OnPackageListInteraction {
    // Constants - Log tag
    private static final String TAG = "MainActivity";

    public static final int REQUEST_TRACKING_CODES = 1;
    public static final int REQUEST_BARCODE = 2;

    @Nullable
    @Bind(R.id.info_fragment_container)
    FrameLayout infoFragmentContainer;
    @Nullable
    @Bind(R.id.unselected_view)
    View unselectedView;

    PackageInfoFragment mInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_main);
        setTitle(R.string.title_activity_main);
        ButterKnife.bind(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private NZPostTrackedPackage selectedPackage;

    @Override
    public void onItemClicked(NZPostTrackedPackage trackedPackage) {
        selectedPackage = trackedPackage;
        boolean displayTwoPanes = getResources().getBoolean(R.bool.display_two_panes);
        if (displayTwoPanes) {
            showPackageInInfoFragment(trackedPackage);
        } else {
            Intent intent = new Intent(this, PackageInfoActivity.class);
            intent.putExtra(PackageInfoActivity.PACKAGE_PARCEL, trackedPackage);
            startActivity(intent);
        }
    }

    private void showPackageInInfoFragment(NZPostTrackedPackage trackedPackage) {
        if (unselectedView.getVisibility() != View.GONE) {
            unselectedView.setVisibility(View.GONE);
        }
        if (infoFragmentContainer.getVisibility() != View.VISIBLE) {
            infoFragmentContainer.setVisibility(View.VISIBLE);
        }
        mInfoFragment = PackageInfoFragment.newInstance(trackedPackage);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)

                .replace(R.id.info_fragment_container, mInfoFragment)
                .commit();
    }

    @Override
    public void requestManualEntry(@Nullable String code) {

    }

    @Override
    public void requestBarcode() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                swipeRefreshLayout.setRefreshing(true);
//                refreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
//                refreshTask.execute();
//                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

//            case R.id.action_clear_done:
//                final List<String> packagesToDelete = db.getDeliveredPackageCodes();
//                if (packagesToDelete.size() == 0) return false;
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle(R.string.title_delete_packages)
//                        .setMessage(MainActivity.this.getString(R.string.message_delete_all_delivered_packages))
//                        .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                for (String code : packagesToDelete) {
//                                    db.deletePackage(code);
//                                }
//                                updateItems();
//                                dialog.dismiss();
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

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        db.open();
//        switch (requestCode) {
//
//            case REQUEST_BARCODE:
//                if (resultCode == RESULT_OK) {
//                    String code = data.getStringExtra(BarcodeScannerUtil.EXTRA_SCAN_RESULT);
//                    if (!CodeValidationUtil.isValidCode(code)) {
//                        new AlertDialog.Builder(this)
//                                .setTitle(R.string.title_error)
//                                .setMessage(Html.fromHtml(getString(R.string.error_code_scanned_invalid, code)))
//                                .setPositiveButton(R.string.dialog_button_try_again, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Intent zxingIntent = new Intent(BarcodeScannerUtil.ZXING_SCAN_ACTIVITY_NAME);
//                                        startActivityForResult(zxingIntent, REQUEST_BARCODE);
//                                        dialog.dismiss();
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                })
//                                .show();
//                    } else {
//                        Intent intent = new Intent(this, CodeInputActivity.class);
//                        intent.putExtra(CodeInputActivity.CODE, code);
//                        startActivityForResult(intent, REQUEST_TRACKING_CODES);
//                    }
//                } else if (resultCode == RESULT_CANCELED) {
//                    return;
//                } else {
//                    new AlertDialog.Builder(this)
//                            .setTitle(R.string.title_error)
//                            .setMessage(R.string.message_zxing_error)
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                }
//                break;
//
//        }
//    }

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
