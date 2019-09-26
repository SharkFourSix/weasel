package app.weasel.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import app.weasel.interfaces.OnAdapterSelectionChangedListener;

abstract class BaseFragment extends Fragment {
    OnAdapterSelectionChangedListener mAdapterSelectionChangedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof OnAdapterSelectionChangedListener)) {
            throw new IllegalArgumentException(
                context.getClass()
                    + " must implement " + OnAdapterSelectionChangedListener.class
            );
        }
        this.mAdapterSelectionChangedListener = (OnAdapterSelectionChangedListener) context;
    }
}