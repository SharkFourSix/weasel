package app.weasel.adapter.util;

import android.widget.ViewFlipper;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <p>Given a {@link RecyclerView.Adapter}, {@link androidx.appcompat.widget.AppCompatTextView},
 * and a {@link ViewFlipper}, this class observes the
 * adapter and automatically switches the view flipper's child views
 * based on the state of the adapter and updates the text view's text content to reflect the
 * adapter's item count</p>
 */
public final class AutoViewSwitcherAdapterObserver extends RecyclerView.AdapterDataObserver {
    private final ViewFlipper mViewFlipper;
    private final int mEmptyViewIndex, mDataViewIndex;
    private final RecyclerView.Adapter mAdapter;
    private final AppCompatTextView mStatusText;

    public AutoViewSwitcherAdapterObserver(
        RecyclerView.Adapter adapter,
        ViewFlipper viewFlipper,
        AppCompatTextView statusText,
        int emptyViewIndex,
        int dataViewIndex) {

        this.mViewFlipper = viewFlipper;
        this.mStatusText = statusText;
        this.mEmptyViewIndex = emptyViewIndex;
        this.mDataViewIndex = dataViewIndex;
        this.mAdapter = adapter;
    }

    public void register() {
        mAdapter.registerAdapterDataObserver(this);
    }

    public void unregister() {
        mAdapter.unregisterAdapterDataObserver(this);
    }

    @Override
    public void onChanged() {
        onAdapterStateChanged();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        onAdapterStateChanged();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        onAdapterStateChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        onAdapterStateChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        onAdapterStateChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        onAdapterStateChanged();
    }

    private void onAdapterStateChanged() {
        mViewFlipper.setDisplayedChild(
            mAdapter.getItemCount() == 0 ? mEmptyViewIndex : mDataViewIndex
        );
        mStatusText.setText(String.format(Locale.US, "%,d", mAdapter.getItemCount()));
    }
}
