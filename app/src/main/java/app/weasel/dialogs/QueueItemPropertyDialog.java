package app.weasel.dialogs;

import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import app.weasel.R;
import app.weasel.databinding.DialogQueueItemBinding;
import app.weasel.event.EventPublisher;
import app.weasel.event.QueueEvent;
import app.weasel.model.QueueItem;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class QueueItemPropertyDialog extends BottomSheetDialog {

    public QueueItemPropertyDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_Light_BottomSheetDialog_Adaptive);
        init(context);
    }

    public void show(QueueItem queueItem) {
        mBinding.setQueueItem(mQueueItem = queueItem);
        super.show();
    }

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_queue_item,
            null,
            false
        );
        setContentView(mBinding.getRoot());
        ButterKnife.bind(this);
    }

    @OnClick(R.id.download_button)
    void moveToDownloads() {
        EventPublisher.publish(new QueueEvent(QueueEvent.Type.download_item, mQueueItem));
        dismiss();
    }

    private QueueItem mQueueItem;
    private DialogQueueItemBinding mBinding;
}
