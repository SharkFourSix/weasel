package app.weasel.persistence.dao;

import java.util.List;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import app.weasel.model.DownloadItem;

@Dao
public interface DownloadsDao extends BaseDao<DownloadItem> {

    @Query("SELECT * FROM downloads ORDER BY status")
    DataSource.Factory<Integer, DownloadItem> stream();

    @Query("SELECT * FROM downloads WHERE fileName LIKE :value")
    DataSource.Factory<Integer, DownloadItem> filteredStream(String value);

    @Query("SELECT * FROM downloads ORDER BY status")
    List<DownloadItem> getAll();

    @Query("SELECT * FROM downloads WHERE songId = :songId")
    DownloadItem getBySongId(long songId);

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY status LIMIT 1")
    DownloadItem getNextPending(DownloadItem.DownloadStatus status);

    @Query("UPDATE downloads SET status = :pending WHERE status = :downloading")
    void resetDownloadsToPending(DownloadItem.DownloadStatus pending,
                                 DownloadItem.DownloadStatus downloading);

    @Update
    void update(DownloadItem item);

    @Delete
    void delete(List<DownloadItem> items);

    @Insert
    void save(DownloadItem item);
}
