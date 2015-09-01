package com.anrapps.spotkeeper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeoutException;


public class ActivitySettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getFragmentManager().beginTransaction().replace(R.id.content, new PreferenceFragment()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void launch(Activity from, boolean finish) {
        from.startActivity(new Intent(from, ActivitySettings.class));
        if (finish) from.finish();
    }

    public static class PreferenceFragment extends android.preference.PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_general);
            bindPreferenceSummaryToValue(findPreference("output_format"));
            bindPreferenceSummaryToValue(findPreference("audio_encoder"));
            bindPreferenceSummaryToValue(findPreference("encoding_bitrate"));
            bindPreferenceSummaryToValue(findPreference("sampling_rate"));

            int res = getActivity().checkCallingOrSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
            boolean granted = res == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                Preference systemPref = findPreference("move_to_system");
                systemPref.setSummary(R.string.preference_summary_move_to_system_disabled);
                findPreference("move_to_system").setEnabled(false);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {
            if (preference.getKey().equals("move_to_system")) {
                new MoveToSystemTask(getActivity()).execute();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class MoveToSystemTask extends AsyncTask<Void, Void, Void> {

        private static final String[] COMMANDS = new String[] {
                "mount -o remount,rw /system /system",
                "cp -rp %s /system/priv-app/SpotKeeper",
        };

        private final WeakReference<Activity> mContextRef;

        private boolean exception;
        private boolean rootDenied;

        public MoveToSystemTask(Activity context) {
            this.mContextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (new File("/system/priv-app/SpotKeeper/base.apk").exists()) {
                    if (BuildConfig.DEBUG)
                        Log.d(getClass().getName(), "File is already present in system. Skipping...");
                    return null; //Already exists
                }
                String path = "/data/app/com.anrapps.spotkeeper-1";
                File file = new File(path);
                if (!file.exists()) {
                    if (BuildConfig.DEBUG)
                        Log.w(getClass().getName(), "File " + path + " does not exist");
                    path = "/data/app/com.anrapps.spotkeeper-2";
                }
                file = new File(path);
                if (!file.exists()) {
                    if (BuildConfig.DEBUG)
                        Log.w(getClass().getName(), "File " + path + " does not exist neither. Return");
                    exception = true;
                    return null;
                }
                COMMANDS[1] = String.format(COMMANDS[1], path);

                Command command = new Command(0, COMMANDS) {
                    @Override public void commandOutput(int id, String line) {
                        if (BuildConfig.DEBUG) Log.d("commandOutput", line);
                        super.commandOutput(id, line);
                    }
                    @Override public void commandTerminated(int id, String reason) {
                        if (BuildConfig.DEBUG) Log.d("commandTerminated", reason);
                    }
                    @Override public void commandCompleted(int id, int exitCode) {
                        if (BuildConfig.DEBUG) Log.d("commandCompleted", "Exit with code: " + exitCode);
                    }
                };
                RootShell.getShell(true).add(command);
                RootShell.closeAllShells();
            } catch (TimeoutException | RootDeniedException | IOException e) {
                e.printStackTrace();
                Log.e(getClass().getName(), e.getMessage());
                exception = true;
                rootDenied = e instanceof RootDeniedException;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mContextRef.get() == null) return; //no need to give feedback
            if (!exception)
                Toast.makeText(mContextRef.get(), R.string.text_move_to_system_success, Toast.LENGTH_SHORT).show();
            else {
                if (rootDenied) Toast.makeText(mContextRef.get(), R.string.text_root_access_denied, Toast.LENGTH_SHORT).show();
                else Toast.makeText(mContextRef.get(), R.string.text_error_accessing_root, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ?
                        listPreference.getEntries()[index] :
                        null);
            } else preference.setSummary(stringValue);
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference == null) {
            if (BuildConfig.DEBUG)
                Log.w(ActivitySettings.class.getName(), "Preference is null, skipping bind");
            return;
        }
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

}
