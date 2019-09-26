package app.weasel.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.LongSparseArray;
import androidx.core.app.NotificationCompat;
import app.weasel.R;
import app.weasel.activity.BootstrapActivity;
import app.weasel.activity.MainActivity;
import app.weasel.event.DownloadItemEvent;
import app.weasel.event.EventPublisher;
import app.weasel.event.ExitApplicationEvent;
import app.weasel.event.NewQueueItemEvent;
import app.weasel.event.QueueEvent;
import app.weasel.event.RelaunchEvent;
import app.weasel.event.SongDiscoveryEvent;
import app.weasel.model.DownloadItem;
import app.weasel.model.LibraryItem;
import app.weasel.model.QueueItem;
import app.weasel.persistence.database.ApplicationDatabase;
import app.weasel.persistence.repository.DownloadsRepository;
import app.weasel.persistence.repository.LibraryRepository;
import app.weasel.persistence.repository.QueueRepository;
import app.weasel.persistence.repository.Repositories;
import app.weasel.util.MediaUtils;
import app.weasel.util.PlatformUtils;
import app.weasel.util.PreferenceUtils;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class DownloadService extends Service {

    private static final String ACTION_EXIT_APPLICATION = "app.weasel.intent.action.EXIT_APPLICATION";

    private static final String SERVICE_CHANNEL_ID = "service_channel";

    private static final String TAG = "DownloadService";

    private static final int SERVICE_NOTIFICATION_ID = 101;

    public static final class ExitIntentReceiver extends BroadcastReceiver {
        public ExitIntentReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_EXIT_APPLICATION.equalsIgnoreCase(intent.getAction())) {
                EventPublisher.publish(new ExitApplicationEvent());
                mService.tearDownService();
            }
        }
    }

    private NotificationCompat.Builder mServiceNotificationBuilder;

    private NotificationManager mNotificationManager;
    private RemoteViews mServiceNotificationRemoteViews;

    private static volatile DownloadService mService;

    private Handler mHandler;

    private final Runnable mIdleIndicator = () -> setStatusText(getString(R.string.service_idle));

    private final CompositeDisposable mDisposableContainer = new CompositeDisposable();

    private ApplicationDatabase mDatabase;

    /**
     * Tasks that are currently downloading go in here
     */
    private final LongSparseArray<Future<File>> mDownloadTasks = new LongSparseArray<>();
    private final AtomicInteger mTaskCount = new AtomicInteger();

    @Override
    public void onCreate() {
        mService = this;
        mHandler = new Handler();

        initializeNotificationComponents();
        setStatusText(getString(R.string.service_idle));

        mDatabase = ApplicationDatabase.getInstance(getApplication());

        EventPublisher.register(this);

        if (PreferenceUtils.isAutomaticDownloadEnabled(this)) {
            new Thread(() -> {
                // Because Malawi Music servers don't support resumable downloads, once the downloads
                // get cancelled mid flight, the downloads would have had to start from the beginning
                //
                // So to avoid time wasting checks, we simply reset all downloads to pending and
                // treat them all as pending since we'd have to start all over any way.
                mDatabase.downloadsDao().resetDownloadsToPending(
                    DownloadItem.DownloadStatus.pending,
                    DownloadItem.DownloadStatus.downloading
                );

                // start downloads
                downloadNextPendingFile(false);
            }).start();
        }
    }

    private synchronized void tearDownService() {
        // Cancel downloads right away gracefully (hint)
        Ion.getDefault(this).cancelAll();

        // Disable clicking the notification
        mServiceNotificationRemoteViews.setOnClickPendingIntent(R.id.container, null);
        mServiceNotificationRemoteViews.setViewVisibility(R.id.exit_button, View.GONE);
        mServiceNotificationRemoteViews.setTextViewText(R.id.content, getString(R.string.exiting));
        mNotificationManager.notify(SERVICE_NOTIFICATION_ID, mServiceNotificationBuilder.build());

        // Wait for all the tasks to complete
        new Thread(() -> {
            ExecutorService service = Ion.getIoExecutorService();

            // The final blow
            service.shutdownNow();

            while (!service.isTerminated()) {
                try {
                    Ion.getIoExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Interruption in teardown thread", e);
                }
            }

            // Finally stop the service
            stopSelf();
        }).start();
    }

    @WorkerThread
    private void downloadNextPendingFile(boolean async) {
        Runnable runnable = () -> {
            final DownloadItem item = mDatabase.downloadsDao()
                .getNextPending(DownloadItem.DownloadStatus.pending);

            if (item != null) {
                downloadFile(this, item);
            }
        };
        if (async) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void getFileInformation(NewQueueItemEvent event) {
        Single.just(event.getIds())
            .subscribeOn(Schedulers.computation())
            .subscribeOn(Schedulers.io())
            .subscribe(new SingleObserver<long[]>() {
                Disposable disposable;

                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onSuccess(long[] longs) {
                    disposable.dispose();

                    final Context context = DownloadService.this;

                    for (final long songId : longs) {
                        if (songId <= 0) continue;

                        final boolean isSongDiscovered =
                            mDatabase.queueDao().getBySongId(songId) != null ||
                                mDatabase.downloadsDao().getBySongId(songId) != null ||
                                mDatabase.libraryDao().getBySongId(songId) != null;

                        if (isSongDiscovered) {
                            Log.v(TAG, "Song with ID " + songId + " already discovered");
                            continue;
                        }

                        final String url = "http://m.malawi-music.com/download/index.php?song=" + songId;

                        Ion.with(context)
                            .load("HEAD", url)
                            .setTimeout(PreferenceUtils.getHttpTimeout(context))
                            .userAgent(PreferenceUtils.getUserAgent(context))
                            .asString()
                            .withResponse()
                            .setCallback((e, result) -> {
                                if (e == null) {
                                    final HeadersResponse response = result.getHeaders();
                                    final Headers headers = response.getHeaders();
                                    if (response.code() == 200) {
                                        final long fileSize = Long.valueOf(headers.get("Content-Length"));
                                        final String disposition = headers.get("Content-Disposition");
                                        String fileName = null;

                                        if (disposition != null) {
                                            final int index = disposition.indexOf("filename=\"");
                                            if (index > -1) {
                                                fileName = disposition.substring(index + 10,
                                                    disposition.lastIndexOf('"'));
                                                fileName = PlatformUtils.escapeFileName(fileName);
                                            }
                                        }

                                        if (fileName == null) {
                                            // fall back to song id as the file name
                                            fileName = String.format(Locale.US, "%d.mp3", songId);
                                        }

                                        final QueueItem queueItem = new QueueItem();

                                        queueItem.setCreated(new Date());
                                        queueItem.setUrl(url);
                                        queueItem.setFileName(fileName);
                                        queueItem.setFileSize(fileSize);
                                        queueItem.setSongId(songId);
                                        queueItem.setSavePath(
                                            new File(
                                                PlatformUtils.getDownloadsDirectory(context),
                                                fileName
                                            ).getAbsolutePath()
                                        );

                                        Repositories.of(
                                            QueueRepository.class,
                                            mDatabase
                                        ).save(queueItem);

                                        EventBus.getDefault().post(new SongDiscoveryEvent(fileName));
                                    } else {
                                        logMetadataRetrievalError(
                                            result.getException()
                                        );
                                    }
                                } else {
                                    logMetadataRetrievalError(e);
                                }
                            });
                    }
                }

                @Override
                public void onError(Throwable e) {
                }
            });
    }

    private void logMetadataRetrievalError(Exception e) {
        Log.v(TAG, "SONG METADATA ERROR: " + e.getMessage(), e);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSongDiscovered(SongDiscoveryEvent event) {
        if (event.getEventCode() == SongDiscoveryEvent.EventCode.song_discovered) {
            mHandler.removeCallbacks(mIdleIndicator);
            setStatusText(event.getSongTitle());
            mHandler.postDelayed(mIdleIndicator, TimeUnit.SECONDS.toMillis(5));
        }
    }

    /**
     * This handler method moves discovered items to downloads, for downloading
     *
     * @param event Event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onQueueEvent(QueueEvent event) {

        DownloadItem downloadItem = new DownloadItem();
        downloadItem.setStatus(DownloadItem.DownloadStatus.pending);
        downloadItem.setUrl(event.getItem().getUrl());
        downloadItem.setDownloaded(0);
        downloadItem.setCreated(new Date());
        downloadItem.setSongId(event.getItem().getSongId());
        downloadItem.setFileName(event.getItem().getFileName());
        downloadItem.setSavePath(event.getItem().getSavePath());
        downloadItem.setDownloadId(0);
        downloadItem.setFileSize(event.getItem().getFileSize());

        // remove from discovery queue
        mDatabase.queueDao().delete(event.getItems());

        // Add to download queue
        mDatabase.downloadsDao().save(downloadItem);
    }

    private void updateDownloadItem(DownloadItem item) {
        if (!PlatformUtils.isCurrentThreadMainThread()) {
            mDatabase.downloadsDao().update(item);
        } else {
            Repositories.of(DownloadsRepository.class, mDatabase).update(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDownloadItemEvent(DownloadItemEvent event) {
        final DownloadItem item = event.getItem();
        switch (event.getType()) {
            case stop:
                if (!cancelDownload(item)) {
                    item.setStatus(DownloadItem.DownloadStatus.cancelled);
                    updateDownloadItem(item);
                }
                break;
            case download:
                if (mTaskCount.get() > PlatformUtils.MAX_THREAD_COUNT
                    && PreferenceUtils.isConcurrentLimitEnabled(this)) {
                    mHandler.post(() -> Toast.makeText(this,
                        getString(R.string.max_theads_reached),
                        Toast.LENGTH_SHORT).show());
                } else {
                    downloadFile(this, item);
                }
                break;
        }
    }

    private synchronized boolean cancelDownload(DownloadItem item) {
        final Future<File> task = mDownloadTasks.get(item.getId());
        if (task != null) {
            task.cancel(true);
            return true;
        }
        return false;
    }

    private synchronized void downloadFile(Context context, DownloadItem item) {
        // increment tasks
        mTaskCount.incrementAndGet();

        item.setStatus(DownloadItem.DownloadStatus.downloading);
        updateDownloadItem(item);

        mDownloadTasks.put(
            item.getId(),
            Ion.with(context)
                .load(item.getUrl())
                .setTimeout(PreferenceUtils.getHttpTimeout(context))
                .addHeader("User-Agent", PreferenceUtils.getUserAgent(context))
                .progress((downloaded, total) -> {
                    item.setDownloaded(downloaded);
                    mDatabase.downloadsDao().update(item);
                })
                .write(PlatformUtils.normalizeSavePath(item.getSavePath()))
                .setCallback((e, result) -> {
                    if (e != null) {
                        Log.e(TAG, "DOWNLOAD ERROR: "
                            + item.getFileName() + ". " + e.getMessage(), e);
                        item.setStatus(DownloadItem.DownloadStatus.failed);
                        updateDownloadItem(item);
                    } else {
                        MediaUtils.SongInfo songInfo = MediaUtils
                            .getLocalFileMetadata(result.getAbsolutePath());

                        final LibraryItem libraryItem = new LibraryItem();

                        libraryItem.setAlbum(songInfo.getAlbum());
                        libraryItem.setAlbumArt(songInfo.getAlbumArt());
                        libraryItem.setArtist(songInfo.getArtist());
                        libraryItem.setDuration(songInfo.getDuration());
                        libraryItem.setTitle(songInfo.getTitle());

                        libraryItem.setFilePath(result.getAbsolutePath());
                        libraryItem.setFileSize(item.getFileSize());
                        libraryItem.setSongId(item.getSongId());
                        libraryItem.setCreated(new Date());

                        // Remove from the downloads
                        Repositories.of(DownloadsRepository.class, mDatabase)
                            .delete(new ArrayList<DownloadItem>() {{
                                add(item);
                            }});

                        // Yay! We have a song
                        Repositories.of(
                            LibraryRepository.class,
                            mDatabase
                        ).save(libraryItem);
                    }

                    // Remove this task since it's done
                    synchronized (mDownloadTasks) {
                        mDownloadTasks.remove(item.getId());
                    }

                    // decrement task count
                    mTaskCount.decrementAndGet();

                    // Move to the next file
                    if (PreferenceUtils.isAutomaticDownloadEnabled(context)) {
                        downloadNextPendingFile(true);
                    }
                })
        );
    }

    /**
     * <p>Will return true if the service is running or throw a NullPointerException</p>
     *
     * <p>This method is simply used to test whether or not the service instance is still around</p>
     *
     * @return true
     */
    private boolean probeInstance() {
        return true;
    }

    public static boolean isServiceRunning() {
        try {
            return mService != null && mService.probeInstance();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        mService = null;
        mDisposableContainer.dispose();
        stopForeground(true);
        mNotificationManager.cancelAll();
        EventBus.getDefault().unregister(this);
        mDatabase.close();
        Log.v(TAG, "onDestroy");
    }

    private NotificationCompat.Builder newBuilder(String channel, RemoteViews views) {
        return new NotificationCompat
            .Builder(this, channel)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(false)
            .setContent(views);
    }

    @SuppressWarnings({"ConstantConditions"})
    private void initializeNotificationComponents() {
        mServiceNotificationRemoteViews = new RemoteViews(
            getPackageName(),
            R.layout.notification_service_status);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mServiceNotificationBuilder = newBuilder(
            SERVICE_CHANNEL_ID,
            mServiceNotificationRemoteViews
        );

        mServiceNotificationRemoteViews.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        mServiceNotificationRemoteViews.setImageViewResource(R.id.exit_button, R.drawable.ic_cross);

        mServiceNotificationRemoteViews.setOnClickPendingIntent(
            R.id.container,
            PendingIntent
                .getActivity(
                    this,
                    101,
                    new Intent(
                        this,
                        MainActivity.class
                    ).setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    ),
                    0
                )
        );

        mServiceNotificationRemoteViews
            .setOnClickPendingIntent(
                R.id.exit_button,
                PendingIntent
                    .getBroadcast(
                        this,
                        100,
                        new Intent(
                            this,
                            ExitIntentReceiver.class
                        ).setAction(
                            ACTION_EXIT_APPLICATION
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
            );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String[][] channels = {
                {SERVICE_CHANNEL_ID, getString(R.string.service_channel), getString(R.string.service_description), "false"},
            };
            final int[] importance = {
                NotificationManager.IMPORTANCE_LOW,
            };
            final boolean[] vibration = {false};
            for (int i = 0; i < channels.length; i++) {
                final String[] channel_info = channels[i];
                final int channel_importance = importance[i];
                final NotificationChannel channel = new NotificationChannel(channel_info[0],
                    channel_info[1], channel_importance);
                channel.setDescription(channel_info[2]);
                channel.setShowBadge(Boolean.valueOf(channel_info[3]));
                channel.enableVibration(vibration[i]);
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        startForeground(SERVICE_NOTIFICATION_ID, mServiceNotificationBuilder.build());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void relaunchActivity(RelaunchEvent event) {
        EventPublisher.publish(new ExitApplicationEvent());
        startActivity(
            new Intent(
                this,
                BootstrapActivity.class
            ).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            ).putExtra(
                MainActivity.EXTRA_FRAGMENT,
                event.getFragment()
            )
        );
    }

    private void setStatusText(CharSequence text) {
        mServiceNotificationRemoteViews.setTextViewText(R.id.content, text);
        mNotificationManager.notify(SERVICE_NOTIFICATION_ID, mServiceNotificationBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
