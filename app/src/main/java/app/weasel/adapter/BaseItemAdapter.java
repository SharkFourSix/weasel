package app.weasel.adapter;

import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import app.weasel.R;
import app.weasel.adapter.util.ChangePayload;
import app.weasel.model.Item;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Base {@link Item item} adapter containing all the boilerplate logic
 *
 * @param <T> Type of item the extending adapter encapsulates
 */
public abstract class BaseItemAdapter<T extends Item>
    extends PagedListAdapter<T, BaseItemAdapter.ItemViewHolder<T>> {

    /**
     * <p>Callback for communicating item selection state updates and item diffing</p>
     *
     * @see DiffUtil.ItemCallback
     */
    public interface Callback<T extends Item> {

        /**
         * <p>Called when selection mode changes</p>
         *
         * @param enabled True if the adapter is in selection mode
         * @see #isInSelectionMode()
         * @see #setInSelectionMode(boolean)
         */
        void onSelectionStatusChanged(boolean enabled);

        /**
         * <p>Called when selection count changed</p>
         *
         * @param count Current selection count
         */
        void onSelectionCountChanged(int count);

        /**
         * <p>Called when an item was selected</p>
         *
         * @param item The selected item
         */
        void onItemSelected(T item);

        boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);

        boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem);

        /**
         * <p>Return the items attributes that have changed</p>
         *
         * @param oldItem Old item
         * @param newItem New item
         * @return List of changes
         * @see DiffUtil.ItemCallback#getChangePayload(Object, Object)
         */
        @Nullable
        ChangePayload getChangePayload(@NonNull T oldItem, @NonNull T newItem);
    }

    private boolean mInSelectionMode;
    private Callback<T> mCallback;
    private final SparseBooleanArray mSelectionKeys;

    BaseItemAdapter(Callback<T> callback) {
        super(new DiffUtil.ItemCallback<T>() {
            @Override
            public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return callback.areItemsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return callback.areContentsTheSame(oldItem, newItem);
            }

            @Nullable
            @Override
            public ChangePayload getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
                return callback.getChangePayload(oldItem, newItem);
            }
        });
        setCallback(callback);
        mSelectionKeys = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public final ItemViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createHolder(parent, viewType);
    }

    @Override
    public final void onBindViewHolder(@NonNull ItemViewHolder<T> holder, int position) {
        holder.bind0(position, Collections.emptyList());
    }

    @Override
    public final void onBindViewHolder(@NonNull ItemViewHolder<T> holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.bind0(position, payloads);
        }
    }

    public final Callback<T> getCallback() {
        return mCallback;
    }

    public final void setCallback(Callback<T> callback) {
        this.mCallback = Objects.requireNonNull(callback, "Callback cannot be null");
    }

    public final boolean isInSelectionMode() {
        return mInSelectionMode;
    }

    public final void setInSelectionMode(boolean b) {
        if (b != this.mInSelectionMode) {
            this.mInSelectionMode = b;
            mSelectionKeys.clear();
            notifyDataSetChanged();
            mCallback.onSelectionStatusChanged(b);
        }
    }

    private boolean isItemSelected(int position) {
        return mSelectionKeys.get(position, false);
    }

    /**
     * <p>Toggle the selection state of the item at the given position</p>
     *
     * @param position The item's position
     */
    private void toggleItemSelection(int position) {
        if (mSelectionKeys.get(position, false)) {
            mSelectionKeys.delete(position);
        } else {
            mSelectionKeys.put(position, true);
        }
        notifyItemChanged(position);
        mCallback.onSelectionCountChanged(mSelectionKeys.size());
    }

    public final List<T> getSelectedItems() {
        final ArrayList<T> selectedItems = new ArrayList<>();
        for (int i = 0; i < mSelectionKeys.size(); i++) {
            selectedItems.add(getItem(mSelectionKeys.keyAt(i)));
        }
        return selectedItems;
    }

    @Override
    public final int getItemCount() {
        return super.getItemCount();
    }

    protected final T getItem(int position) {
        return super.getItem(position);
    }

    /**
     * Implement this method to create a {@link ItemViewHolder view holdler}
     *
     * @param parent   Parent container
     * @param viewType View type as returned by {@link #getItemViewType(int)}
     * @return A view holder of type {@link ItemViewHolder}
     * @see #onCreateViewHolder(ViewGroup, int)
     */
    protected abstract ItemViewHolder<T> createHolder(ViewGroup parent, int viewType);

    static abstract class ItemViewHolder<T extends Item> extends RecyclerView.ViewHolder {

        @BindView(R.id.checkbox)
        AppCompatCheckBox checkbox;

        BaseItemAdapter<T> baseItemAdapter;

        ItemViewHolder(BaseItemAdapter<T> adapter, @NonNull View itemView) {
            super(itemView);
            this.baseItemAdapter = adapter;
        }

        /**
         * Call this method from derived holder classes to bind views using ButterKnife
         */
        final void bindViews() {
            ButterKnife.bind(this, itemView);
        }

        /**
         * Item selection trigger.
         *
         * <p>Items are selectable only when the adapter is not is selection mode</p>
         */
        @OnClick(R.id.item)
        final void onClick() {
            final int pos = getAdapterPosition();
            if (pos >= 0) {
                if (baseItemAdapter.isInSelectionMode()) {
                    baseItemAdapter.toggleItemSelection(pos);
                } else {
                    baseItemAdapter.mCallback.onItemSelected(baseItemAdapter.getItem(pos));
                }
            }
        }

        /**
         * Selection trigger method
         */
        @OnLongClick(R.id.item)
        final void onLongClick() {
            final int idx = getAdapterPosition();
            if (idx >= 0) {
                if (!baseItemAdapter.isInSelectionMode()) {
                    baseItemAdapter.setInSelectionMode(true);
                    baseItemAdapter.toggleItemSelection(idx);
                }
            }
        }

        /**
         * This method binds the common/shared views
         *
         * @param position Item position
         */
        private void bind0(int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                checkbox.setVisibility(baseItemAdapter.isInSelectionMode() ? View.VISIBLE : View.GONE);
                checkbox.setChecked(baseItemAdapter.isItemSelected(position));
            }
            bind(position, payloads);
        }

        /**
         * Derived {@link ItemViewHolder holder} classes override this method to bind non-shared views
         *
         * @param position Item position
         * @param payloads Payloads for partial update. Perform a full bind if the list is empty
         * @see androidx.recyclerview.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int, List)
         */
        abstract void bind(int position, List<Object> payloads);
    }
}
