package app.weasel.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import app.weasel.R;
import app.weasel.databinding.LibraryItemBinding;
import app.weasel.model.LibraryItem;

public final class LibraryAdapter extends BaseItemAdapter<LibraryItem> {

    public LibraryAdapter(Callback<LibraryItem> callback) {
        super(callback);
    }

    @Override
    protected ItemViewHolder<LibraryItem> createHolder(ViewGroup parent, int viewType) {
        return new Holder(
            this,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.library_item,
                parent,
                false
            )
        );
    }

    private class Holder extends ItemViewHolder<LibraryItem> {
        private final LibraryItemBinding binding;


        Holder(BaseItemAdapter<LibraryItem> adapter, LibraryItemBinding binding) {
            super(adapter, binding.getRoot());
            this.binding = binding;
            super.bindViews();
        }

        @Override
        void bind(int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                binding.setSong(getItem(position));
            }
        }
    }
}
