package app.weasel.adapter.util;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.weasel.adapter.BaseItemAdapter;
import app.weasel.model.Item;

public class CallbackAdapter<T extends Item> implements BaseItemAdapter.Callback<T> {
    @Override
    public void onSelectionStatusChanged(boolean enabled) {

    }

    @Override
    public void onSelectionCountChanged(int count) {

    }

    @Override
    public void onItemSelected(T item) {

    }

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return Objects.equals(oldItem, newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem == newItem;
    }

    @Nullable
    @Override
    public ChangePayload getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
        return null;
    }
}
