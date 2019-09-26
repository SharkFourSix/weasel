package app.weasel.persistence.dao;

import java.util.List;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import app.weasel.model.LibraryItem;

@Dao
public interface LibraryDao extends BaseDao<LibraryItem> {

    @Query("SELECT * FROM library")
    DataSource.Factory<Integer, LibraryItem> stream();

    @Query("SELECT * FROM library WHERE title LIKE :value OR artist LIKE :value")
    DataSource.Factory<Integer, LibraryItem> filteredStream(String value);

    @Query("SELECT * FROM library WHERE songId = :songId")
    LibraryItem getBySongId(long songId);

    @Query("SELECT * FROM library")
    List<LibraryItem> getAll();

    @Insert
    void save(LibraryItem item);

    @Delete
    void delete(List<LibraryItem> items);

    @Update
    void update(LibraryItem item);
}
