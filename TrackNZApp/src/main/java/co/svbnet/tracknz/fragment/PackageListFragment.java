package co.svbnet.tracknz.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import co.svbnet.tracknz.activity.CodeInputActivity;
import co.svbnet.tracknz.activity.PackageInfoActivity;
import co.svbnet.tracknz.adapter.TrackedPackagesArrayAdapter;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tasks.PackageUpdateTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.util.BarcodeScannerUtil;
import co.svbnet.tracknz.util.CodeValidationUtil;

public class PackageListFragment extends Fragment {

    @Bind(R.id.add_button) FloatingActionsMenu addFloatingButton;
    @Bind(R.id.fab_new_from_clipboard) FloatingActionButton pasteFloatingButton;
    @Bind(R.id.packages_container) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.packages) ListView listView;

    // Data references
    private TrackingDB db = new TrackingDB(getContext());
    private TrackedPackagesArrayAdapter adapter;
    private List<NZPostTrackedPackage> adapterItems = new ArrayList<>();

    // Refresh task reference
    private MainPackageRefreshTask refreshTask;

    private OnPackageListInteraction mListener;

    public PackageListFragment() {
        // Required empty public constructor
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_package_info, container, false);
        ButterKnife.bind(this, view);
        // setup our packages swipe refresh layout
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTask = new MainPackageRefreshTask(new NZPostTrackingService());
                refreshTask.execute();
            }
        });

        // create packages list view adapter
        adapter = new TrackedPackagesArrayAdapter(getContext(), adapterItems);
        listView.setAdapter(adapter);
        //listView.setMultiChoiceModeListener(new PackagesMultiChoiceListener());

        // update set view items
        adapterItems.addAll(db.findAllPackages());
        adapter.notifyDataSetChanged();
        return view;
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

    @OnItemClick(R.id.packages)
    public void onPackagesListViewItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemClicked(adapterItems.get(position));
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
            getView().findViewById(R.id.empty_list_text).setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    private void updateItems() {
        updateWidgetStates();
        adapter.notifyDataSetChanged();
    }

    public interface OnPackageListInteraction {
        void onItemClicked(NZPostTrackedPackage trackedPackage);
        void requestManualEntry(@Nullable String code);
        void requestBarcode();
    }

    private class MainPackageRefreshTask extends PackageUpdateTask {

        /**
         * Constructs a new instance of a MainPackageRefreshTask for this activity.
         */
        public MainPackageRefreshTask(NZPostTrackingService service) {
            super(service, PackageListFragment.this.getContext(), PackageListFragment.this.db, null);
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
            for (NZPostTrackedPackage item : adapterItems) {
                for (NZPostTrackedPackage uitem : updatedPackages) {
                    if (item.getTrackingCode().equals(uitem.getTrackingCode())) {
                        adapterItems.set(adapterItems.indexOf(item), uitem);
//                        updatedPackages.remove(uitem);
                    }
                }
            }
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

}
