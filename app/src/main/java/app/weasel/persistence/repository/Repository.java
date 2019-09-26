package app.weasel.persistence.repository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.paging.DataSource;
import app.weasel.model.Item;
import app.weasel.persistence.dao.BaseDao;

public abstract class Repository<T extends Item> {
    private final Executor mExecutor;
    private final BaseDao<T> mDao;

    Repository(BaseDao<T> dao) {
        mExecutor = Executors.newSingleThreadExecutor();
        this.mDao = dao;
    }

    public final void update(T item) {
        mExecutor.execute(() -> mDao.update(item));
    }

    public final void delete(List<T> items) {
        mExecutor.execute(() -> mDao.delete(items));
    }

    public final void save(T item) {
        mExecutor.execute(() -> mDao.save(item));
    }

    public final T getBySongId(long songId) {
        return mDao.getBySongId(songId);
    }

    public final DataSource.Factory<Integer, T> stream() {
        return mDao.stream();
    }

    public final DataSource.Factory<Integer, T> filteredStream(String filter) {
        return mDao.filteredStream(filter);
    }
}