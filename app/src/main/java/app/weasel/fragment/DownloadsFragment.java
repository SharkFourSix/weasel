package app.weasel.fragment;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.weasel.adapter.BaseItemAdapter;
import app.weasel.adapter.DownloadAdapter;
import app.weasel.adapter.util.CallbackAdapter;
import app.weasel.adapter.util.ChangePayload;
import app.weasel.event.DownloadItemEvent;
import app.weasel.event.EventPublisher;
import app.weasel.model.DownloadItem;
import app.weasel.persistence.viewmodel.DownloadsViewModel;
import app.weasel.util.PlatformUtils;
import io.reactivex.Completable;

public final class DownloadsFragment extends ItemFragment<DownloadItem, DownloadsViewModel> {
    private static final String TAG = "DownloadsFragment";

    private DownloadAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DownloadAdapter(new CallbackAdapter<DownloadItem>() {
            @Override
            public boolean areContentsTheSame(@NonNull DownloadItem oldItem, @NonNull DownloadItem newItem) {
                return oldItem.getDownloaded() == newItem.getDownloaded()
                    && oldItem.getStatus() == newItem.getStatus();
            }

            @Override
            public void onItemSelected(DownloadItem item) {
                Log.v(TAG, item.toString());
            }

            @Override
            public ChangePayload getChangePayload(@NonNull DownloadItem oldItem, @NonNull DownloadItem newItem) {
                return new DownloadAdapter.DownloadStatusPayload(newItem.getStatus(), newItem.getProgress());
            }
        });
    }

    @Override
    BaseItemAdapter<DownloadItem> getItemAdapter() {
        return mAdapter;
    }

    @Override
    DownloadsViewModel getViewModel() {
        return getViewModel(DownloadsViewModel.class);
    }

    @Override
    boolean usesAddFabButton() {
        return false;
    }

    @Override
    Completable onBeforeItemsAreDeleted(List<DownloadItem> items) {
        return Completable.fromAction(() -> {
            for (final DownloadItem item : items) {
                EventPublisher.publish(new DownloadItemEvent(DownloadItemEvent.Type.stop, item));
                PlatformUtils.deleteFile(new File(item.getSavePath()));
            }
        });
    }
}
