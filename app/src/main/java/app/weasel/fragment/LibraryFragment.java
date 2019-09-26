package app.weasel.fragment;

import android.os.Bundle;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.weasel.R;
import app.weasel.activity.SongPropertySheetActivity;
import app.weasel.adapter.BaseItemAdapter;
import app.weasel.adapter.LibraryAdapter;
import app.weasel.adapter.util.CallbackAdapter;
import app.weasel.model.LibraryItem;
import app.weasel.persistence.viewmodel.LibraryViewModel;
import app.weasel.util.PlatformUtils;
import butterknife.BindString;
import io.reactivex.Completable;

public final class LibraryFragment extends ItemFragment<LibraryItem, LibraryViewModel> {

    @BindString(R.string.empty_library)
    String emptyString;

    private LibraryAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new LibraryAdapter(new CallbackAdapter<LibraryItem>() {
            @Override
            public boolean areContentsTheSame(@NonNull LibraryItem oldItem, @NonNull LibraryItem newItem) {
                return areItemsTheSame(oldItem, newItem);
            }

            @Override
            public void onItemSelected(LibraryItem item) {
                // noinspection ConstantConditions
                SongPropertySheetActivity.viewItem(getContext(), item);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyViewText(emptyString);
    }

    @Override
    BaseItemAdapter<LibraryItem> getItemAdapter() {
        return mAdapter;
    }

    @Override
    LibraryViewModel getViewModel() {
        return getViewModel(LibraryViewModel.class);
    }

    @Override
    boolean usesAddFabButton() {
        return false;
    }

    @Override
    Completable onBeforeItemsAreDeleted(List<LibraryItem> items) {
        return Completable.fromAction(() -> {
            for (final LibraryItem item : items) {
                PlatformUtils.deleteFile(new File(item.getFilePath()));
            }
        });
    }
}
