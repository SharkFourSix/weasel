package app.weasel.persistence.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import app.weasel.model.Item;
import app.weasel.persistence.repository.Repository;

public abstract class BaseViewModel<T extends Item> extends AndroidViewModel {
    private Repository<T> mRepository;
    private final LiveData<PagedList<T>> stream;
    private final MutableLiveData<String> mSearchQueryLiveData = new MutableLiveData<>();

    private final LiveData<PagedList<T>> mSearchResultLiveData;

    private static final int PAGE_SIZE = 20;

    BaseViewModel(Repository<T> repository, @NonNull Application application) {
        super(application);
        stream = new LivePagedListBuilder<>((mRepository = repository).stream(), PAGE_SIZE).build();
        mSearchResultLiveData = Transformations.switchMap(
            mSearchQueryLiveData,
            input -> new LivePagedListBuilder<>(
                mRepository.filteredStream(input),
                PAGE_SIZE
            ).build()
        );
    }

    public final T getSongById(long songId) {
        return mRepository.getBySongId(songId);
    }

    public final void delete(List<T> items) {
        mRepository.delete(items);
    }

    public final void update(T item) {
        mRepository.update(item);
    }

    public final LiveData<PagedList<T>> stream() {
        return stream;
    }

    public final LiveData<PagedList<T>> filteredStream() {
        return mSearchResultLiveData;
    }

    public final void submitQuery(String query) {
        mSearchQueryLiveData.setValue(query);
    }

    public final void save(T item) {
        mRepository.save(item);
    }
}
