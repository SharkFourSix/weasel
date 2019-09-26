package app.weasel.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import app.weasel.BuildConfig;
import app.weasel.R;

public enum PlatformUtils {
    ;
    private static final String TAG = "PlatformUtils";

    public static final int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static boolean isCurrentThreadMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static void setNightMode(boolean b) {
        AppCompatDelegate.setDefaultNightMode(
            b ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static File getBaseDirectory(Context context) {
        File dir = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name));
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.e(TAG, "Failed to create home directory on shared storage");
            }
        }
        return dir;
    }

    public static File getDatabaseFilePath(Context context) {
        return new File(getDatabaseDirectory(context), context.getString(R.string.app_name));
    }

    public static File makeUpdatePath(Context context, String version) {
        return new File(
            getSharedDirectory(context),
            String.format(
                Locale.US,
                "%s.%s.apk",
                context.getString(R.string.app_name),
                version
            ).toLowerCase()
        );
    }

    public static File getDatabaseDirectory(Context context) {
        File dir = new File(getBaseDirectory(context), ".database");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create database directory");
            }
        }
        return dir;
    }

    /**
     * Remove reserved characters from the given file name
     *
     * @param fileName File name to normalize
     * @return A normalized file name without reserved characters
     */
    public static String escapeFileName(String fileName) {
        return fileName.replaceAll("[?<>:|\\\\*]", "_");
    }

    public static File normalizeSavePath(String fileName) {
        final File file = new File(fileName);
        if (!file.getParentFile().mkdirs()) {
            Log.w(TAG, "mkdirs: false");
        }
        return file;
    }

    public static File getDownloadsDirectory(Context context) {
        File dir = new File(getBaseDirectory(context), "downloads");
        if (!dir.mkdirs()) {
            Log.e(TAG, "Failed to create downloads directory");
        }
        // TODO Add ".nomedia" support (if requested)
        return dir;
    }

    private static String getApplicationPath(Context context) {
        try {
            return context.getPackageManager()
                .getApplicationInfo(context.getPackageName(), 0).sourceDir;
        } catch (Exception e) {
            return null;
        }
    }

    public static File getSharedDirectory(Context context) {
        File dir = new File(getCacheDirectory(context), ".shared");
        if (!dir.exists()) {
            boolean dummy = dir.mkdirs();
        }
        return dir;
    }

    private static File getCacheDirectory(Context context) {
        return context.getExternalCacheDir();
    }

    public static void shareApplication(Context context) {
        final File applicationPath = new File(getApplicationPath(context));
        final File temp = new File(getSharedDirectory(context),
            String.format(Locale.US, "%s.%s.apk",
                context.getString(R.string.app_name), BuildConfig.VERSION_NAME));

        if (!temp.exists()) {
            copyFile(applicationPath, temp);
        }

        Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, temp);

        Intent share_intent = new Intent(Intent.ACTION_SEND);
        share_intent.putExtra(Intent.EXTRA_STREAM, uri);
        share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share_intent.setType("application/vnd.android.package-archive");

        context.startActivity(Intent.createChooser(share_intent, "Share application"));
    }

    public static void deleteFile(File file) {
        if (!file.delete()) {
            Log.e(TAG, "DELETE ERROR: " + file.getAbsolutePath());
        }
    }

    public static void selectPlayer(Context context, String file) {
        Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, new File(file));
        context.startActivity(
            Intent.createChooser(
                new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "audio/mp3")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                "Open with..."
            )
        );
    }

    @SuppressWarnings({"UnusedReturnValue", "ResultOfMethodCallIgnored"})
    private static boolean copyFile(File src, File dst) {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        FileChannel inFileChannel = null;
        FileChannel outFileChannel = null;
        boolean status = false;
        try {
            if (!dst.exists()) {
                if (!dst.getParentFile().exists()) {
                    dst.getParentFile().mkdirs();
                }
                dst.createNewFile();
            }
            outFileChannel = (fos = new FileOutputStream(dst)).getChannel();
            inFileChannel = (fis = new FileInputStream(src)).getChannel();
            inFileChannel.transferTo(0, inFileChannel.size(), outFileChannel);
            status = true;
        } catch (Exception e) {
            Log.e("copyFile", "E: " + e.getMessage(), e);
        } finally {
            closeChannel(inFileChannel);
            closeChannel(outFileChannel);
            closeCloseable(fos);
            closeCloseable(fis);
        }
        return status;
    }

    private static void closeChannel(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                //
            }
        }
    }

    private static void closeCloseable(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                //
            }
        }
    }
}
