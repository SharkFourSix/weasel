package app.weasel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import app.weasel.R;
import app.weasel.databinding.QueueItemBinding;
import app.weasel.event.EventPublisher;
import app.weasel.event.QueueEvent;
import app.weasel.model.QueueItem;
import butterknife.BindView;
import butterknife.OnClick;

public final class QueueAdapter extends BaseItemAdapter<QueueItem> {

    public QueueAdapter(Callback<QueueItem> callback) {
        super(callback);
    }

    @Override
    protected ItemViewHolder<QueueItem> createHolder(ViewGroup parent, int viewType) {
        return new Holder(
            this,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.queue_item,
                parent,
                false
            )
        );
    }

    class Holder extends ItemViewHolder<QueueItem> {
        private final QueueItemBinding binding;

        @BindView(R.id.download_button)
        View mDownloadButton;

        Holder(BaseItemAdapter<QueueItem> adapter, QueueItemBinding binding) {
            super(adapter, binding.getRoot());
            this.binding = binding;
            super.bindViews();
        }

        @OnClick(R.id.download_button)
        void downloadSong() {
            EventPublisher.publish(
                new QueueEvent(
                    QueueEvent.Type.download_item,
                    getItem(getAdapterPosition())
                )
            );
        }

        @Override
        void bind(int position, List<Object> payloads) {
            binding.setItem(getItem(position));
            mDownloadButton.setVisibility(isInSelectionMode() ? View.GONE : View.VISIBLE);
        }
    }
}
