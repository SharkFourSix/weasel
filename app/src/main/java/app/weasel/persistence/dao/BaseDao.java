package app.weasel.persistence.dao;

import java.util.List;

import androidx.paging.DataSource;
import app.weasel.model.Item;

public interface BaseDao<T extends Item> {
    void delete(List<T> items);

    void update(T item);

    DataSource.Factory<Integer, T> stream();

    DataSource.Factory<Integer, T> filteredStream(String value);

    /**
     * This is meant to be called on the same thread as the caller's
     *
     * @param songId .
     * @return .
     */
    T getBySongId(long songId);

    void save(T item);
}
