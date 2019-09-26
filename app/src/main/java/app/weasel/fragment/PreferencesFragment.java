package app.weasel.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import app.weasel.BuildConfig;
import app.weasel.R;
import app.weasel.activity.AttributionActivity;
import app.weasel.activity.MainActivity;
import app.weasel.event.EventPublisher;
import app.weasel.event.RelaunchEvent;
import app.weasel.interfaces.OnCancelAdapterSelectionListener;
import app.weasel.util.PlatformUtils;
import app.weasel.util.PreferenceUtils;
import app.weasel.util.UpdateUtils;

public final class PreferencesFragment extends PreferenceFragmentCompat
    implements OnCancelAdapterSelectionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "PreferencesFragment";

    private Preference mUpdateDownloadPreference;

    @SuppressWarnings({"ConstantConditions"})
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setRetainInstance(true);
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("pref_version").setSummary(
            String.format(
                Locale.US,
                "%s <<%s>>",
                BuildConfig.VERSION_NAME,
                new Date(BuildConfig.BUILD_TIMESTAMP)
            )
        );
        findPreference("pref_home_path")
            .setSummary(PlatformUtils.getBaseDirectory(getContext()).getAbsolutePath());

        ((SwitchPreference) findPreference(getString(R.string.pref_concurrent_download_limit)))
            .setSummaryOn(
                String.format(
                    Locale.US,
                    "%d concurrent downloads max",
                    PlatformUtils.MAX_THREAD_COUNT
                )
            );

        mUpdateDownloadPreference = findPreference("pref_update_download");

        setupUpdateDownloadPreference();

        getPreferenceManager()
            .getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    private void setupUpdateDownloadPreference() {
        if (PreferenceUtils.isUpdateAvailable(getContext())) {
            mUpdateDownloadPreference.setSummary(
                "New version is " + PreferenceUtils.getNewApplicationVersionTag(getContext())
            );
            mUpdateDownloadPreference.setVisible(true);
        } else {
            mUpdateDownloadPreference.setVisible(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceManager()
            .getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case "night_mode":
                // FIXME There's a glitch where colors don't fully change when using the native API to
                //  recreate the application. We simply use our own method
                EventPublisher.publish(new RelaunchEvent(MainActivity.FRAGMENT_INDEX_PREFERENCES));
                break;
            case "pref_licenses":
                // We could have used the <intent /> tag in the preferences XML but since the package
                // name is different for debug and release version, we'd have to resort to
                // using static string resources which could potentially break this functionality
                // during refactoring.
                // So the following will suffice
                startActivity(new Intent(getContext(), AttributionActivity.class));
                break;
            case "pref_share":
                PlatformUtils.shareApplication(getContext());
                break;
            case "pref_home_path":
                ((ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText(
                        "Base directory",
                        preference.getSummary()
                    ));
                Toast.makeText(getContext(),
                    "Copied to clipboard", Toast.LENGTH_SHORT).show();
                break;
            case "pref_download_mode_info":
                new AlertDialog.Builder(getContext())
                    .setMessage(R.string.download_mode_help_info)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
                break;
            case "pref_update_download":
                UpdateUtils.downloadLatestApplication(getContext());
                break;
            case "pref_update_check":
                UpdateUtils.checkForApplicationUpdate(getContext());
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onCancelAdapterSelection() {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("update_available".equalsIgnoreCase(key)) {
            setupUpdateDownloadPreference();
        }
    }
}
