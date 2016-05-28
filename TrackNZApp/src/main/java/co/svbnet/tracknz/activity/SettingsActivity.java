package co.svbnet.tracknz.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import co.svbnet.tracknz.BackgroundRefreshManager;
import co.svbnet.tracknz.BuildConfig;
import co.svbnet.tracknz.PreferenceKeys;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.ui.ToolbarActivity;

/**
 * Activity which enables the user to edit the app's preferences.
 */
public class SettingsActivity extends ToolbarActivity {

    private static final String TAG = SettingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            if (BuildConfig.DEBUG) {
                // Base debug category
                PreferenceCategory debugCategory = new PreferenceCategory(getActivity());
                debugCategory.setTitle("Debug utils");
                getPreferenceScreen().addPreference(debugCategory);

                // Insert dummy codes preference
//                Preference debugItemsPreference = new Preference(getActivity());
//                debugItemsPreference.setTitle("Insert dummy codes");
//                debugItemsPreference.setSummary("Dummy codes w/out invalid code");
//                debugItemsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        new DebugPackageRetrieveTask(new NZPostTrackingService());
//                        return true;
//                    }
//                });
//                debugCategory.addPreference(debugItemsPreference);

                // Test notifications
                Preference testNotificationPreference = new Preference(getActivity());
                testNotificationPreference.setTitle("Test notifications");
                testNotificationPreference.setSummary("Notifications now please.");
                testNotificationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent alarmIntent = new Intent(BackgroundRefreshManager.INTENT_START_ALARM);
                        getActivity().getApplication().sendBroadcast(alarmIntent);
                        return true;
                    }
                });
                debugCategory.addPreference(testNotificationPreference);

                // Reset DB
                Preference resetDbPreference = new Preference(getActivity());
                resetDbPreference.setTitle("Reset DB");
                resetDbPreference.setSummary("Drops DB tables and recreates");
                resetDbPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        TrackingDB db = new TrackingDB(getActivity());
                        db.open();
                        db.reset();
                        Toast.makeText(getActivity(), "DB reset success", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                debugCategory.addPreference(resetDbPreference);

            }
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceKeys.NOTIFICATIONS_ENABLED:
                    new BackgroundRefreshManager(getActivity()).setFromPreferences(sharedPreferences);
                    break;
            }
        }

//        private class DebugPackageRetrieveTask extends PackageRetrievalTask {
//
//            private ProgressDialog progressDialog;
//            private TrackingDB db;
//
//            public DebugPackageRetrieveTask(NZPostTrackingService service) {
//                super(service);
//                progressDialog = new ProgressDialog(getContext());
//                progressDialog.setMessage(getString(R.string.message_getting_package_information));
//                progressDialog.setCancelable(false);
//                db = new TrackingDB(getContext());
//            }
//
//            @Override
//            protected void onPreExecute() {
//                progressDialog.show();
//                super.onPreExecute();
//            }
//
//            @Override
//            protected List<NZPostTrackedPackage> doInBackground(String... params) {
//                List<String> codes = null;
//                try {
//                    codes = new NZPostTrackingService()._retrieveDebugCodes();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                String[] ccArray = new String[codes.size()];
//                codes.toArray(ccArray);
//                return super.doInBackground(ccArray);
//            }
//
//            @Override
//            protected void onException(Exception ex) {
//                String errorMessage = getString(R.string.message_unknown_error,
//                        ex.getClass().getName(), ex.getMessage());
//                if (ex instanceof ConnectException) {
//                    errorMessage = getString(R.string.message_error_no_connection);
//                }
//                new AlertDialog.Builder(getContext())
//                        .setTitle(R.string.title_error)
//                        .setMessage(errorMessage)
//                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//            }
//
//            @Override
//            protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
//                progressDialog.hide();
//                super.onPostExecute(trackedPackages);
//            }
//
//            @Override
//            protected void onSuccess(List<NZPostTrackedPackage> retrievedPackages) {
//                for (final NZPostTrackedPackage retrievedPackage : retrievedPackages) {
//                    if (retrievedPackage.getErrorCode() != null) {
//                        if (retrievedPackage.getErrorCode().equals("N")) {
//                            new AlertDialog.Builder(getContext())
//                                    .setTitle(R.string.title_nzp_error)
//                                    .setMessage(getString(R.string.message_add_nonexistent_package, retrievedPackage.getTrackingCode()))
//                                    .setPositiveButton(R.string.dialog_button_yes_add, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            db.insertPackage(retrievedPackage);
//                                            dialog.dismiss();
//                                        }
//                                    })
//                                    .setNegativeButton(R.string.dialog_button_dont_add, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.cancel();
//                                        }
//                                    })
//                                    .show();
//                        } else {
//                            new AlertDialog.Builder(getContext())
//                                    .setTitle(R.string.title_nzp_error)
//                                    .setMessage(retrievedPackage.getDetailedStatus())
//                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                        }
//                                    })
//                                    .show();
//                        }
//                    } else {
//                        db.insertPackage(retrievedPackage);
//                    }
//                }
//            }
//        }
    }
}
