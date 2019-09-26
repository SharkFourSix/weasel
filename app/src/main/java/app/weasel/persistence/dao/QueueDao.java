package app.weasel.persistence.dao;

import java.util.List;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import app.weasel.model.QueueItem;

@Dao
public interface QueueDao extends BaseDao<QueueItem> {

    @Query("SELECT * FROM queue")
    DataSource.Factory<Integer, QueueItem> stream();

    @Query("SELECT * FROM queue WHERE fileName LIKE :value")
    DataSource.Factory<Integer, QueueItem> filteredStream(String value);

    @Query("SELECT * FROM queue WHERE songId = :songId")
    QueueItem getBySongId(long songId);

    @Delete
    void delete(List<QueueItem> items);

    @Update
    void update(QueueItem item);

    @Insert
    void save(QueueItem item);
}
