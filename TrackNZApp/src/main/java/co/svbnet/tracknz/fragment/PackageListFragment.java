package co.svbnet.tracknz.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.util.CodeValidationUtil;
import co.svbnet.tracknz.util.PackageModifyUtil;
import co.svbnet.tracknz.util.ShareUtil;

public class PackageListFragment extends Fragment {

    /* View bindings */
    @Bind(R.id.add_button) FloatingActionsMenu addFloatingButton;
    @Bind(R.id.fab_new_from_clipboard) FloatingActionButton pasteFloatingButton;
    @Bind(R.id.packages_container) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.packages) ListView listView;

    /* Private fields */
    private TrackingDB mDb;
    private TrackedPackagesArrayAdapter mAdapter;
    private List<NZPostTrackedPackage> mAdapterItems = new ArrayList<>();
    private MainPackageRefreshTask mRefreshTask;
    private OnPackageListInteraction mListener;

    /* Fragment methods */
    public PackageListFragment() {
        // Required empty public constructor
    }

    public interface OnPackageListInteraction {
        void onItemSelected(NZPostTrackedPackage trackedPackage);
        void onSelectedItemChanged();
        void onSelectedItemDeleted();
        void requestManualEntry(@Nullable String code);
        void requestBarcode();
    }

    public static PackageListFragment newInstance() {
        // No init arguments
        return new PackageListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPackageListInteraction) {
            mListener = (OnPackageListInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPackageListInteraction");
        }
        mDb = new TrackingDB(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mDb.close();
        mDb = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // create packages list view adapter
        mAdapter = new TrackedPackagesArrayAdapter(getContext(), mAdapterItems);
        // update set view items
        mAdapterItems.addAll(mDb.findAllPackages());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("PackageListFragment", "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_package_list, container, false);
        ButterKnife.bind(this, view);
        // setup our packages swipe refresh layout
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
                mRefreshTask.execute();
            }
        });
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new PackagesMultiChoiceListener());
        mAdapter.notifyDataSetChanged();

        // check if we can show the paste button
        checkIfCodeIsOnClipboard();
        updateWidgetStates(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                initiateRefresh();
                break;

            case R.id.action_clear_done:
                initiateClearAllDelivered();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isVisible()) {
            inflater.inflate(R.menu.menu_package_list, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @OnClick(R.id.fab_enter)
    public void onFabEnterClick(View v) {
        mListener.requestManualEntry(null);
        // Make sure FAB menu is collapsed after we start an activity
        addFloatingButton.collapse();
    }

    @OnClick(R.id.fab_scan)
    public void onFabScanClick(View v) {
        mListener.requestBarcode();
        addFloatingButton.collapse();
    }

    @OnClick(R.id.fab_new_from_clipboard)
    public void onFabPasteClick(View v) {
        String code = getClipboardTrackingCode();
        if (code == null) return;
        mListener.requestManualEntry(code);
        addFloatingButton.collapse();
    }

    private View lastSelected = null;
    private NZPostTrackedPackage mSelectedPackage;

    @OnItemClick(R.id.packages)
    public void onPackagesListViewItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (getContext().getResources().getBoolean(R.bool.is_tablet) &&
//                getContext().getResources().getBoolean(R.bool.is_landscape)) {
//            if (lastSelected != null) {
//                lastSelected.setBackgroundResource(android.R.color.transparent);
//            }
//            view.setBackgroundResource(R.color.selection);
//            lastSelected = view;
//        }
        mSelectedPackage = mAdapterItems.get(position);
        mListener.onItemSelected(mSelectedPackage);
    }


    private String getClipboardTrackingCode() {
        ClipboardManager clipboardManager = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (primaryClip != null) {
            String text = primaryClip.getItemAt(0).coerceToText(getContext()).toString();
            if (CodeValidationUtil.isValidCode(text)) {
                return text;
            }
        }
        return null;
    }

    private void initiateRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        mRefreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
        mRefreshTask.execute();
    }

    private void initiateClearAllDelivered() {
        final List<String> packagesToDelete = mDb.getDeliveredPackageCodes();
        if (packagesToDelete.size() == 0) return;
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_delete_packages)
                .setMessage(getContext().getString(R.string.message_delete_all_delivered_packages))
                .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (String code : packagesToDelete) {
                            mDb.deletePackage(code);
                        }
                        updateItems();
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
    private void updateWidgetStates(View view) {
        if (mAdapterItems.size() > 0) {
            view.findViewById(R.id.empty_list_text).setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    private void updateItems() {
        updateWidgetStates(getView());
        mAdapter.notifyDataSetChanged();
    }

    public List<NZPostTrackedPackage> getItems() {
        return mAdapterItems;
    }

    public void invalidateItems() {
        updateItems();
    }

    public void selectItem(int index) {
//        View view = mAdapter.getView(index, null, listView);
//        if (!getContext().getResources().getBoolean(R.bool.is_tablet) ||
//                !getContext().getResources().getBoolean(R.bool.is_landscape)) {
//            if (lastSelected != null) {
//                lastSelected.setBackgroundResource(android.R.color.transparent);
//            }
//            view.setBackgroundResource(R.color.selection);
//            lastSelected = view;
//        }
        mSelectedPackage = mAdapterItems.get(index);
    }

    public NZPostTrackedPackage getSelectedPackage() {
        return mSelectedPackage;
    }

    private class MainPackageRefreshTask extends PackageUpdateTask {

        /**
         * Constructs a new instance of a MainPackageRefreshTask for this activity.
         */
        public MainPackageRefreshTask(NZPostTrackingService service) {
            super(service, PackageListFragment.this.getContext(), PackageListFragment.this.mDb);
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
            if (error instanceof ConnectException || error instanceof UnknownHostException) {
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
            String selectedCode = mSelectedPackage != null ? mSelectedPackage.getTrackingCode() : null;
            for (NZPostTrackedPackage item : updatedPackages) {
                int idx = -1;
                for (int i = 0; i < mAdapterItems.size(); i++) {
                    if (mAdapterItems.get(i).getTrackingCode().equals(item.getTrackingCode())) {
                        idx = i;
                        break;
                    }
                }
                mAdapterItems.set(idx, item);
                if (selectedCode != null && selectedCode.equals(item.getTrackingCode())) {
                    mSelectedPackage = item;
                    mListener.onSelectedItemChanged();
                }
            }
//            for (NZPostTrackedPackage item : mAdapterItems) {
//                for (NZPostTrackedPackage uitem : updatedPackages) {
//                    if (item.getTrackingCode().equals(uitem.getTrackingCode())) {
//
//                        mAdapterItems.set(mAdapterItems.indexOf(item), uitem);
//                        if (context.getResources().getBoolean(R.bool.is_tablet) &&
//                                context.getResources().getBoolean(R.bool.is_landscape)) {
//                            if (item.getTrackingCode().equals(selectedCode)) {
//                                mListener.onItemSelected(uitem);
//                            }
//                        }
////                        updatedPackages.remove(uitem);
//                    }
//                }
//            }

            updateItems();
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
    private class PackagesMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

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
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_packages_contextual, menu);
            // Hide FAB when in CAB mode
            addFloatingButton.setVisibility(View.GONE);
            // Stop refreshing
            if (mRefreshTask != null) {
                mRefreshTask.cancel(true);
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
                    final NZPostTrackedPackage trackedPackage = mAdapterItems.get(itemId);
                    PackageModifyUtil.editLabel(getContext(), mDb, trackedPackage, new PackageModifyUtil.LabelEditComplete() {
                        @Override
                        public void onLabelEditComplete(String newLabel) {
                            mode.finish();
                            updateItems();
                        }
                    });
                    break;

                case R.id.action_share:
                    List<Integer> shareIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
                    String codes = "";
                    for (Integer id : shareIds) {
                            codes += mAdapterItems.get(id).getTrackingCode() + "\n";
                    }
                    ShareUtil.shareCode(getActivity(), codes);
                    break;

                case R.id.action_delete:
                    final List<Integer> delItemIds = getIndicesOfCheckedItems(listView.getCheckedItemPositions());
                    final List<NZPostTrackedPackage> packagesToDelete = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    for (Integer id : delItemIds) {
                        NZPostTrackedPackage selectedItem = mAdapterItems.get(id);
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
                        msg = getContext().getString(R.string.message_delete_package, sb.toString()).replace("<br>", "");
                    } else {
                        msg = getContext().getString(R.string.message_delete_packages, sb.toString());
                    }
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.title_delete_packages)
                            .setMessage(Html.fromHtml(msg))
                            .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (NZPostTrackedPackage id : packagesToDelete) {
                                        if (id.equals(mSelectedPackage)) {
                                            mListener.onSelectedItemDeleted();
                                        }
                                        mDb.deletePackage(id.getTrackingCode());
                                        mAdapterItems.remove(id);

                                    }
                                    updateItems();
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

}
