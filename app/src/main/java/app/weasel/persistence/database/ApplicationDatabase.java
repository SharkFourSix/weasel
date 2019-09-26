package app.weasel.persistence.database;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import app.weasel.R;
import app.weasel.model.DownloadItem;
import app.weasel.model.LibraryItem;
import app.weasel.model.QueueItem;
import app.weasel.persistence.dao.DownloadsDao;
import app.weasel.persistence.dao.LibraryDao;
import app.weasel.persistence.dao.QueueDao;
import app.weasel.persistence.dao.converter.GenericConverters;
import app.weasel.util.PlatformUtils;

@Database(
    version = 1,
    entities = {
        DownloadItem.class,
        LibraryItem.class,
        QueueItem.class
    },
    exportSchema = false
)
@TypeConverters({GenericConverters.class})
public abstract class ApplicationDatabase extends RoomDatabase {
    private static final String TAG = "ApplicationDatabase";

    public abstract DownloadsDao downloadsDao();

    public abstract LibraryDao libraryDao();

    public abstract QueueDao queueDao();

    public synchronized static ApplicationDatabase getInstance(Application application) {
        synchronized (ApplicationDatabase.class) {
            return INSTANCE == null ? (INSTANCE = build(application)) : INSTANCE;
        }
    }

    private static ApplicationDatabase build(Application application) {
        return Room
            .databaseBuilder(
                application,
                ApplicationDatabase.class,
                getDatabaseFilePath(application)
            )
            // <-- Add migrations as necessary
            .build();
    }

    private static String getDatabaseFilePath(Context context) {
        // There's a bug on Lollipop where the database cannot be saved to any other path but
        // internal
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP
            ? PlatformUtils.getDatabaseFilePath(context).getAbsolutePath()
            : context.getString(R.string.app_name);
    }

    private static ApplicationDatabase INSTANCE;
}
