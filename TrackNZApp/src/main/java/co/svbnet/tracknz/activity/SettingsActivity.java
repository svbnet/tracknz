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
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.ui.ToolbarActivity;

/**
 * Activity which enables the user to edit the app's preferences.
 */
public class SettingsActivity extends ToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            if (BuildConfig.DEBUG) {
                final String[] dummyCodes = new String[]{
                        "AA000111222BB",
                        "XX010101010YY",
                        "AB288037891CD",
                        "CC999999999DD",
                        "ZZ000000000ZZ",
                };

                Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String[] codes = null;
                        Intent intent = new Intent(getActivity(), AddCodeActivity.class);
                        switch (preference.getOrder()) {
                            case 0:
                                codes = dummyCodes;
                                break;

                            case 1:
                                codes = new String[]{"INVALID"};
                                break;

                            case 2:
                                codes = new String[]{"INVALIDEN"};
                                break;
                        }
                        intent.putExtra("co.svbnet.tracknz.DEBUG_DUMMY_CODES", codes);
                        startActivity(intent);
                        return true;
                    }
                };

                // Base debug category
                PreferenceCategory debugCategory = new PreferenceCategory(getActivity());
                debugCategory.setTitle("Debug utils");
                getPreferenceScreen().addPreference(debugCategory);

                // Insert dummy codes preference
                Preference debugItemsPreference = new Preference(getActivity());
                debugItemsPreference.setTitle("Insert dummy codes");
                debugItemsPreference.setSummary("Dummy codes w/out invalid code");
                debugItemsPreference.setOnPreferenceClickListener(listener);
                debugCategory.addPreference(debugItemsPreference);

                // Test invalid handling
                Preference invalidHandlingPreference = new Preference(getActivity());
                invalidHandlingPreference.setTitle("Insert invalid package");
                invalidHandlingPreference.setSummary("Inserts invalid code");
                invalidHandlingPreference.setOnPreferenceClickListener(listener);
                debugCategory.addPreference(invalidHandlingPreference);

                // Rectify invalid
                Preference rectifyInvalidPreference = new Preference(getActivity());
                rectifyInvalidPreference.setTitle("Insert invalid package fix");
                rectifyInvalidPreference.setSummary("Inserts code which makes invalid code valid");
                rectifyInvalidPreference.setOnPreferenceClickListener(listener);
                debugCategory.addPreference(rectifyInvalidPreference);

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

                // Test notifications
                Preference testNotificationPreference = new Preference(getActivity());
                testNotificationPreference.setTitle("Test notifications");
                testNotificationPreference.setSummary("Notifications now please.");
                testNotificationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent alarmIntent = new Intent("co.svbnet.tracknz.START_ALARM");
                        getActivity().getApplication().sendBroadcast(alarmIntent);
                        return true;
                    }
                });
                debugCategory.addPreference(testNotificationPreference);

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
                case "notifications_enabled":
//                    if (sharedPreferences.getString("notifications_interval", null) == null) {
//                        sharedPreferences.edit().putString("notifications_interval", "900000").apply();
//                    }
                case "notifications_interval":
                    new BackgroundRefreshManager(getActivity()).setFromPreferences(sharedPreferences);
                    break;
            }
        }
    }

}
