package app.weasel.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import app.weasel.BuildConfig;
import app.weasel.R;
import app.weasel.net.api.github.Tag;


public enum UpdateUtils {
    ;

    public static void checkForApplicationUpdate(Context context) {
        final String logTag = "UpdateChecker";

        final ProgressDialog progressDialog = ProgressDialog.show(
            context,
            context.getString(R.string.update_check_title),
            context.getString(R.string.update_check_message),
            true,
            false
        );

        final String releaseTagsUrl = String.format(
            Locale.US,
            "https://api.github.com/repos/%s/%s/tags",
            BuildConfig.REPOSITORY_OWNER,
            BuildConfig.REPOSITORY_NAME
        );

        Ion.with(context)
            .load(releaseTagsUrl)
            .setTimeout(PreferenceUtils.getHttpTimeout(context))
            .userAgent(PreferenceUtils.getUserAgent(context))
            .as(new TypeToken<List<Tag>>() {
            })
            .setCallback((exception, tags) -> {
                progressDialog.dismiss();

                if (exception != null) {
                    Log.e(logTag, "Error during update check", exception);
                    showMessageDialog(
                        context,
                        context.getString(R.string.update_check_failed),
                        context.getString(R.string.update_check_fail_message)
                    );
                } else {
                    double currentVersion, latestVersion = currentVersion
                        = Double.valueOf(BuildConfig.VERSION_NAME);
                    Tag latestTag = null;
                    if (!tags.isEmpty()) {
                        for (Tag tag : tags) {
                            double version = 0;
                            try {
                                version = Double.valueOf(
                                    tag.getName().startsWith("v")
                                        ? tag.getName().substring(1)
                                        : tag.getName()
                                );
                            } catch (Exception e) {
                                Log.e(logTag, e.getMessage(), e);
                            }
                            if ((latestVersion = Math.max(latestVersion, version)) > currentVersion) {
                                latestTag = tag;
                            }
                        }
                        if (latestTag != null) {
                            PreferenceUtils
                                .setUpdateDetails(context, true, latestTag.getName());

                            new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.new_version_title))
                                .setMessage(context.getString(R.string.new_version_message,
                                    latestTag.getName()))
                                .setPositiveButton(
                                    context.getString(R.string.update_now_button), (dld, w) -> {
                                        dld.dismiss();
                                        downloadLatestApplication(context);
                                    })
                                .setNegativeButton(android.R.string.no, null)
                                .create()
                                .show();
                        } else {
                            PreferenceUtils.setUpdateDetails(context, false, null);
                            showMessageDialog(
                                context,
                                context.getString(R.string.old_version_title),
                                context.getString(R.string.old_version_message)
                            );
                        }
                    } else {
                        showMessageDialog(
                            context,
                            null,
                            "There were no updates found"
                        );
                    }
                }
            });
    }

    public static void downloadLatestApplication(Context context) {
        ProgressDialog progressDialog = ProgressDialog.show(
            context,
            null,
            context.getString(R.string.update_download_message),
            true,
            false
        );

        final String version = PreferenceUtils.getNewApplicationVersionTag(context);
        final String url = String.format(
            Locale.US,
            "https://github.com/%s/%s/releases/download/%s/app-release.apk",
            BuildConfig.REPOSITORY_OWNER,
            BuildConfig.REPOSITORY_NAME,
            version
        );
        final File targetFile = PlatformUtils.makeUpdatePath(context, version);

        Ion.with(context)
            .load(url)
            .progressDialog(progressDialog)
            .write(targetFile)
            .setCallback((e, result) -> {
                progressDialog.dismiss();

                if (e != null) {
                    showMessageDialog(
                        context,
                        context.getString(R.string.error),
                        context.getString(R.string.update_download_failed)
                    );
                } else {
                    PreferenceUtils.setUpdateDetails(context, false, null);

                    Uri uri = FileProvider
                        .getUriForFile(context, BuildConfig.APPLICATION_ID, targetFile);

                    // trigger package installer
                    context.startActivity(
                        new Intent(
                            Intent.ACTION_VIEW
                        ).setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        ).setDataAndType(
                            uri,
                            "application/vnd.android.package-archive"
                        )
                    );
                }
            });
    }

    private static void showMessageDialog(Context context, CharSequence title, CharSequence message) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .show();
    }
}
