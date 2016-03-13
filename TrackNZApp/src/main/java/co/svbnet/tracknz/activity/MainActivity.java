package co.svbnet.tracknz.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.adapter.TrackedPackagesArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.MenuItemStateChanger;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.BarcodeScannerUtil;
import co.svbnet.tracknz.util.CodeValidationUtil;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;


public class MainActivity extends ToolbarActivity {
    // Constants - Log tag
    private static final String TAG = "MainActivity";

    public static final int REQUEST_TRACKING_CODES = 1;
    public static final int REQUEST_BARCODE = 2;

    private MenuItemStateChanger menuItemStateChanger = new MenuItemStateChanger(new int[] {
            R.id.action_refresh,
            R.id.action_clear_done
    });

    // UI widget references
    private FloatingActionsMenu addFloatingButton;
    private FloatingActionButton pasteFloatingButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;

    // Data references
    private TrackingDB db = new TrackingDB(this);
    private TrackedPackagesArrayAdapter adapter;
    private List<NZPostTrackedPackage> adapterItems = new ArrayList<>();

    // Refresh task reference
    private MainPackageRefreshTask refreshTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_main);
        setTitle(R.string.title_activity_main);
        setupUi();
        checkIfCodeIsOnClipboard();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuItemStateChanger.assign(menu);
        menuItemStateChanger.setItemsEnabled(adapterItems.size() > 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                swipeRefreshLayout.setRefreshing(true);
                refreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
                refreshTask.execute();
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_clear_done:
                final List<String> packagesToDelete = db.getDeliveredPackageCodes();
                if (packagesToDelete.size() == 0) return false;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_delete_packages)
                        .setMessage(MainActivity.this.getString(R.string.message_delete_all_delivered_packages))
                        .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (String code : packagesToDelete) {
                                    db.deletePackage(code);
                                }
                                reloadItems();
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
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadItems();
        checkIfCodeIsOnClipboard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        db.open();
        switch (requestCode) {

            case REQUEST_BARCODE:
                if (resultCode == RESULT_OK) {
                    String code = data.getStringExtra(BarcodeScannerUtil.EXTRA_SCAN_RESULT);
                    if (!CodeValidationUtil.isValidCode(code)) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.title_error)
                                .setMessage(Html.fromHtml(getString(R.string.error_code_scanned_invalid, code)))
                                .setPositiveButton(R.string.dialog_button_try_again, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent zxingIntent = new Intent(BarcodeScannerUtil.ZXING_SCAN_ACTIVITY_NAME);
                                        startActivityForResult(zxingIntent, REQUEST_BARCODE);
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
                } else if (resultCode == RESULT_CANCELED) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_error)
                            .setMessage(R.string.message_zxing_error)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                break;

        }
    }

    /**
     * References widgets from XML and performs initial setup activities.
     */
    private void setupUi() {
        addFloatingButton = (FloatingActionsMenu) findViewById(R.id.add_button);
        // Make sure FAB menu is collapsed after we start an activity
        findViewById(R.id.fab_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CodeInputActivity.class));
                addFloatingButton.collapse();
            }
        });
        findViewById(R.id.fab_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BarcodeScannerUtil.isBarcodeScannerInstalled(getPackageManager())) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.title_zxing_not_installed)
                            .setMessage(R.string.message_zxing_not_installed)
                            .setPositiveButton(R.string.dialog_button_get_app, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent gpmIntent = new Intent(Intent.ACTION_VIEW);
                                    gpmIntent.setData(Uri.parse("market://details?id=com.google.zxing.client.android"));
                                    startActivity(gpmIntent);
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
                    Intent zxingIntent = new Intent(BarcodeScannerUtil.ZXING_SCAN_ACTIVITY_NAME);
                    startActivityForResult(zxingIntent, REQUEST_BARCODE);
                }
                addFloatingButton.collapse();
            }
        });
        pasteFloatingButton = (FloatingActionButton)findViewById(R.id.fab_new_from_clipboard);
        pasteFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = getClipboardTrackingCode();
                if (code == null) return;
                Intent intent = new Intent(MainActivity.this, CodeInputActivity.class);
                intent.putExtra(CodeInputActivity.CODE, code);
                startActivityForResult(intent, REQUEST_TRACKING_CODES);
                addFloatingButton.collapse();
            }
        });

        // setup our packages list view swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.packages_container);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
                refreshTask.execute();
            }
        });

        // create packages list view adapter
        adapter = new TrackedPackagesArrayAdapter(this, adapterItems);
        listView = (ListView)findViewById(R.id.packages);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, PackageInfoActivity.class);
            intent.putExtra(PackageInfoActivity.PACKAGE_PARCEL, adapterItems.get(position));
            startActivity(intent);
            }
        });
        listView.setMultiChoiceModeListener(new PackagesMultiChoiceListener());

        // update list view items
        reloadItems();
    }

    private String getClipboardTrackingCode() {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (primaryClip != null) {
            String text = primaryClip.getItemAt(0).coerceToText(this).toString();
            if (CodeValidationUtil.isValidCode(text)) {
                return text;
            }
        }
        return null;
    }

    private void checkIfCodeIsOnClipboard() {
        if (getClipboardTrackingCode() != null) {
            pasteFloatingButton.setVisibility(View.VISIBLE);
        } else {
            pasteFloatingButton.setVisibility(View.GONE);
        }
    }

    /**
     * Enables or disables menu items based on if there are existing packages.
     */
    private void updateWidgetStates() {
        if (adapterItems.size() > 0) {
            findViewById(R.id.empty_list_text).setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            menuItemStateChanger.setItemsEnabled(true);
        } else {
            findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            menuItemStateChanger.setItemsEnabled(false);
        }
    }

    private void reloadItems() {
        adapterItems.clear();
        adapterItems.addAll(db.findAllPackages());
        updateWidgetStates();
        adapter.notifyDataSetChanged();
    }

    private class PackagesMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

        private MenuItemStateChanger misc = new MenuItemStateChanger(new int[]{ R.id.action_set_label });

        private List<Integer> getIndicesOfCheckedItems(SparseBooleanArray items) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                if (items.valueAt(i)) {
                    indices.add(items.keyAt(i));
                }
            }
            return indices;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int checkedItems = listView.getCheckedItemCount();
            // If no items are selected, don't update to avoid awkward fade-out transition
            if (checkedItems >= 1) {
                mode.setTitle(getString(R.string.actionmode_selected, checkedItems));
                // Apply visibility settings to singular menu items
                misc.setItemsVisible(!(checkedItems > 1));
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_packages_contextual, menu);
            misc.assign(menu);
            // Hide FAB when in CAB mode
            addFloatingButton.setVisibility(View.GONE);
            // Stop refreshing
            if (refreshTask != null) {
                refreshTask.cancel(true);
                swipeRefreshLayout.setRefreshing(false);

            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_set_label:
                    // "There can only be one" - Gwen Stefani, from Hollaback Girl
                    int itemId = getIndicesOfCheckedItems(listView.getCheckedItemPositions()).get(0);
                    final NZPostTrackedPackage trackedPackage = adapterItems.get(itemId);
                    PackageModifyUtil.editLabel(MainActivity.this, db, trackedPackage, new PackageModifyUtil.LabelEditComplete() {
                        @Override
                        public void onLabelEditComplete(String newLabel) {
                            mode.finish();
                            reloadItems();
                        }
                    });
                    break;

                case R.id.action_share:
                    List<Integer> shareIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
                    String codes = "";
                    for (Integer id : shareIds) {
                            codes += adapterItems.get(id).getTrackingCode() + "\n";
                    }
                    ShareUtil.shareCode(MainActivity.this, codes);
                    break;

                case R.id.action_delete:
                    final List<Integer> delItemIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
                    final List<NZPostTrackedPackage> packagesToDelete = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    for (Integer id : delItemIds) {
                        NZPostTrackedPackage selectedItem = adapterItems.get(id);
                        packagesToDelete.add(selectedItem);
                        sb.append("<b>");
                        if (selectedItem.getLabel() == null) {
                            sb.append(selectedItem.getTrackingCode());
                            sb.append("</b>");
                        } else {
                            sb.append(selectedItem.getLabel());
                            sb.append("</b>");
                            sb.append(" (");
                            sb.append(selectedItem.getTrackingCode());
                            sb.append(")");
                        }
                        sb.append("<br>");
                    }
                    sb.append("<br>");
                    int itemsSize = delItemIds.size();
                    String msg;
                    if (itemsSize == 1) {
                        msg = MainActivity.this.getString(R.string.message_delete_package, sb.toString()).replace("<br>", "");
                    } else {
                        msg = MainActivity.this.getString(R.string.message_delete_packages, sb.toString());
                    }
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.title_delete_packages)
                            .setMessage(Html.fromHtml(msg))
                            .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (NZPostTrackedPackage id : packagesToDelete) {
                                            db.deletePackage(id.getTrackingCode());
                                    }
                                    reloadItems();
                                    dialog.dismiss();
                                    mode.finish();
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
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            addFloatingButton.setVisibility(View.VISIBLE);
        }
    }

    private class MainPackageRefreshTask extends PackageUpdateTask {

        /**
         * Constructs a new instance of a MainPackageRefreshTask for this activity.
         */
        public MainPackageRefreshTask(NZPostTrackingService service) {
            super(service, MainActivity.this, MainActivity.this.db, null);
        }

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
            swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(trackedPackages);
        }

        @Override
        protected void onCancelled(List<NZPostTrackedPackage> trackedPackages) {
            swipeRefreshLayout.setRefreshing(false);
            super.onCancelled(trackedPackages);
        }

        @Override
        protected void onError(Exception error) {
            String errorMessage = getString(R.string.message_unknown_error, error.getClass().getName(), error.getMessage());
            if (error instanceof ConnectException) {
                errorMessage = getString(R.string.message_error_no_connection);
            }
            new AlertDialog.Builder(context)
                    .setTitle(R.string.title_error)
                    .setMessage(errorMessage)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected void onPackagesInserted(List<NZPostTrackedPackage> updatedPackages) {
            reloadItems();
        }

        @Override
        protected void onPackageError(List<NZPostTrackedPackage> packagesWithErrors) {
            String packageErrors = "";
            for (final NZPostTrackedPackage item : packagesWithErrors) {
                if (!item.getErrorCode().equals("N")) {
                    String plainMessage = String.format("<b>%s</b>: %s<br />", item.getTrackingCode(), item.getDetailedStatus());
                    packageErrors += plainMessage;
                }
            }
            if (!packageErrors.equals("")) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_nzp_error)
                        .setMessage(Html.fromHtml(packageErrors))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }


}
