package app.weasel.fragment;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.weasel.R;
import app.weasel.activity.BrowserActivity;
import app.weasel.adapter.BaseItemAdapter;
import app.weasel.adapter.QueueAdapter;
import app.weasel.adapter.util.CallbackAdapter;
import app.weasel.dialogs.QueueItemPropertyDialog;
import app.weasel.model.QueueItem;
import app.weasel.persistence.viewmodel.QueueItemViewModel;
import butterknife.BindString;
import butterknife.OnClick;
import io.reactivex.Completable;

public final class QueueFragment extends ItemFragment<QueueItem, QueueItemViewModel> {

    public static final String EXTRA_SHOW_ADD_BUTTON = "showAddButton";

    @BindString(R.string.empty_queue)
    String emptyString;

    private QueueAdapter mAdapter;

    private boolean mUsesAddButton = true;

    private QueueItemPropertyDialog mPropertyDialog;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mUsesAddButton = args == null || args.getBoolean(EXTRA_SHOW_ADD_BUTTON, true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPropertyDialog = new QueueItemPropertyDialog(getContext());
        mAdapter = new QueueAdapter(new CallbackAdapter<QueueItem>() {
            @Override
            public boolean areContentsTheSame(@NonNull QueueItem oldItem, @NonNull QueueItem newItem) {
                return oldItem == newItem;
            }

            @Override
            public void onItemSelected(QueueItem item) {
                mPropertyDialog.show(item);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyViewText(emptyString);
    }

    @Override
    BaseItemAdapter<QueueItem> getItemAdapter() {
        return mAdapter;
    }

    @Override
    QueueItemViewModel getViewModel() {
        return getViewModel(QueueItemViewModel.class);
    }

    @Override
    boolean usesAddFabButton() {
        return mUsesAddButton;
    }

    @OnClick(R.id.add_fab)
    void openBrowserActivity() {
        startActivity(new Intent(getContext(), BrowserActivity.class));
    }

    @Override
    Completable onBeforeItemsAreDeleted(List<QueueItem> items) {
        return null;
    }
}
