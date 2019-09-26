package app.weasel.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;
import app.weasel.R;

public enum PreferenceUtils {
    ;

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getStringOrDefault(Context context, String key, @StringRes int resId) {
        return getPreferences(context).getString(key, context.getString(resId));
    }

    public static String getStringOrDefault(Context context, @StringRes int keyResId,
                                            @StringRes int valueResId) {
        return getStringOrDefault(
            context,
            context.getString(keyResId),
            valueResId
        );
    }

    public static boolean getBoolean(Context context, @StringRes int keyId, boolean def) {
        return getPreferences(context).getBoolean(
            context.getString(keyId),
            def
        );
    }

    public static String getUserAgent(Context context) {
        return getStringOrDefault(
            context,
            R.string.pref_http_user_agent,
            R.string.default_user_agent
        );
    }

    public static String getLanguage(Context context) {
        return getStringOrDefault(
            context,
            R.string.pref_lang,
            R.string.default_language
        );
    }

    public static int getHttpTimeout(Context context) {
        return Integer.valueOf(
            getStringOrDefault(
                context,
                R.string.pref_http_request_timeout,
                R.string.default_request_timeout
            )
        );
    }

    public static boolean isAutomaticDownloadEnabled(Context context) {
        return getBoolean(context, R.string.pref_auto_download, false);
    }

    public static boolean isNightModeEnabled(Context context) {
        return getBoolean(context, R.string.pref_night_mode, false);
    }

    public static boolean isUpdateAvailable(Context context) {
        return getPreferences(context).getBoolean("update_available", false);
    }

    public static boolean isConcurrentLimitEnabled(Context context) {
        return getBoolean(
            context,
            R.string.pref_concurrent_download_limit,
            true
        );
    }

    public static String getNewApplicationVersionTag(Context context) {
        return getPreferences(context).getString("new_version", "");
    }

    public static void setUpdateDetails(Context context, boolean available, String version) {
        getPreferences(context)
            .edit()
            .putString("new_version", version)
            .putBoolean("update_available", available)
            .apply();
    }
}
