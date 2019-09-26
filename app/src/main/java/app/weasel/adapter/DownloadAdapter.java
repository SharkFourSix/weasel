package app.weasel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;
import app.weasel.R;
import app.weasel.adapter.util.ChangePayload;
import app.weasel.databinding.DownloadItemBinding;
import app.weasel.event.DownloadItemEvent;
import app.weasel.event.EventPublisher;
import app.weasel.model.DownloadItem;
import butterknife.BindView;
import butterknife.OnClick;

public final class DownloadAdapter extends BaseItemAdapter<DownloadItem> {
    private static final String TAG = "DownloadAdapter";

    public DownloadAdapter(Callback<DownloadItem> callback) {
        super(callback);
    }

    @Override
    protected ItemViewHolder<DownloadItem> createHolder(ViewGroup parent, int viewType) {
        return new Holder(
            this,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.download_item,
                parent,
                false
            )
        );
    }

    class Holder extends ItemViewHolder<DownloadItem> {

        static final int VIEW_INDEX_PENDING = 0;
        static final int VIEW_INDEX_PROGRESS = 1;
        static final int VIEW_INDEX_CAUTION = 3;

        private final DownloadItemBinding binding;

        Holder(BaseItemAdapter<DownloadItem> adapter, DownloadItemBinding binding) {
            super(adapter, binding.getRoot());
            this.binding = binding;
            super.bindViews();
        }

        @BindView(R.id.status_image)
        AppCompatImageView statusImage;

        @BindView(R.id.status_view)
        ViewFlipper mViewFlipper;

        @BindView(R.id.action_button)
        AppCompatImageView mActionButton;

        @OnClick(R.id.action_button)
        void onActionButtonClicked(View view) {
            DownloadItem item = getItem(getAdapterPosition());

            switch (item.getStatus()) {
                case downloading:
                    EventPublisher.publish(new DownloadItemEvent(DownloadItemEvent.Type.stop, item));
                    break;
                case pending:
                case cancelled:
                case failed:
                case paused:
                    EventPublisher.publish(
                        new DownloadItemEvent(
                            DownloadItemEvent.Type.download,
                            item
                        )
                    );
                    break;
            }
        }

        private void updateStatus(DownloadItem.DownloadStatus status) {
            switch (status) {
                case downloading:
                    mActionButton.setImageResource(R.drawable.ic_pause);
                    mViewFlipper.setDisplayedChild(VIEW_INDEX_PROGRESS);
                    break;
                case paused:
                    mActionButton.setImageResource(R.drawable.ic_play);
                    mViewFlipper.setDisplayedChild(VIEW_INDEX_PROGRESS);
                    break;
                case pending:
                    mActionButton.setImageResource(R.drawable.ic_play);
                    mViewFlipper.setDisplayedChild(VIEW_INDEX_PENDING);
                    statusImage.setImageResource(R.drawable.ic_hourglass);
                    break;
                case failed:
                case cancelled:
                    mActionButton.setImageResource(R.drawable.ic_play);
                    mViewFlipper.setDisplayedChild(VIEW_INDEX_CAUTION);
                    statusImage.setImageResource(R.drawable.ic_warning);
                    break;
            }
        }

        @Override
        public void bind(int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                final DownloadItem item = getItem(getAdapterPosition());
                binding.setItem(item);
                mActionButton.setVisibility(isInSelectionMode() ? View.GONE : View.VISIBLE);
                updateStatus(item.getStatus());
            } else {
                // Perform partial updates. This will let us interact with the item still because
                // it will not animate, which momentarily blocks input
                for (final Object payload : payloads) {
                    if (payload instanceof DownloadStatusPayload) {
                        updateStatus(((DownloadStatusPayload) payload).status);
                        binding.progressBar.setProgress(((DownloadStatusPayload) payload).progress);
                        binding.statusLabel.setText(
                            String.format(
                                Locale.US,
                                "%d%%",
                                ((DownloadStatusPayload) payload).progress
                            )
                        );
                    }
                }
            }
        }
    }

    /**
     * This payload class contains the status and progress of a downloading item.
     *
     * <p>The payload is used for partial updates for faster and smooth UI updates instead
     * binding a whole view</p>
     */
    public static final class DownloadStatusPayload extends ChangePayload {
        private final int progress;
        private final DownloadItem.DownloadStatus status;

        public DownloadStatusPayload(DownloadItem.DownloadStatus status, int progress) {
            this.status = status;
            this.progress = progress;
        }
    }
}