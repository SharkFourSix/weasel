package app.weasel.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import app.weasel.R;
import app.weasel.activity.SearchActivity;
import app.weasel.adapter.BaseItemAdapter;
import app.weasel.adapter.util.AutoViewSwitcherAdapterObserver;
import app.weasel.adapter.util.CallbackAdapter;
import app.weasel.adapter.util.ChangePayload;
import app.weasel.interfaces.OnCancelAdapterSelectionListener;
import app.weasel.model.Item;
import app.weasel.persistence.viewmodel.BaseViewModel;
import app.weasel.persistence.viewmodel.ViewModels;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * <p>This fragment contains boilerplate code since the first 3 fragments use the same layout</p>
 */
public abstract class ItemFragment<T extends Item, M extends BaseViewModel<T>> extends BaseFragment implements OnCancelAdapterSelectionListener {

    private static final String TAG = "ItemFragment";

    public static final String EXTRA_SHOW_SEARCH_MENU = "showSearchMenu";
    public static final String EXTRA_FILTER_MODE = "filterMode";

    @BindString(R.string.empty_results)
    String mEmptyResults;

    @BindView(R.id.empty_view)
    AppCompatTextView mEmptyView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.viewFlipper)
    ViewFlipper mViewFlipper;

    @BindView(R.id.delete_fab)
    FloatingActionButton mDeleteFab;

    @BindView(R.id.add_fab)
    FloatingActionButton mAddFab;

    @BindView(R.id.itemCount)
    AppCompatTextView mItemCount;

    private BaseItemAdapter<T> mAdapter;

    private AutoViewSwitcherAdapterObserver mAdapterObserver;

    private boolean mHasOptionsMenu = false;
    private boolean mFilterMode = false;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mHasOptionsMenu = args == null || args.getBoolean(EXTRA_SHOW_SEARCH_MENU, true);
        mFilterMode = args != null && args.getBoolean(EXTRA_FILTER_MODE, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(mHasOptionsMenu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root_view = getLayoutInflater().inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, root_view);
        return root_view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!mAdapter.isInSelectionMode()) {
            inflater.inflate(R.menu.main, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            // noinspection ConstantConditions, unchecked
            SearchActivity.startActivity(getContext(), this.getClass());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapterObserver.unregister();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!usesAddFabButton()) {
            mAddFab.hide();
        }

        final BaseItemAdapter.Callback<T> delegateCallback = (mAdapter = getItemAdapter()).getCallback();

        // Trampoline hook
        mAdapter.setCallback(
            new CallbackAdapter<T>() {
                @Override
                public void onSelectionStatusChanged(boolean enabled) {
                    mAdapterSelectionChangedListener.onSelectionStateChanged(enabled);
                    if (enabled) {
                        if (usesAddFabButton()) {
                            mAddFab.hide();
                        }
                    } else {
                        mDeleteFab.hide();
                        if (usesAddFabButton()) {
                            mAddFab.show();
                        }
                    }
                    delegateCallback.onSelectionStatusChanged(enabled);
                }

                @Override
                public void onSelectionCountChanged(int count) {
                    mAdapterSelectionChangedListener.onSelectionCountChanged(count);
                    if (count > 0) {
                        mDeleteFab.show();
                    } else {
                        mDeleteFab.hide();
                    }
                    delegateCallback.onSelectionCountChanged(count);
                }

                @Override
                public void onItemSelected(T item) {
                    delegateCallback.onItemSelected(item);
                }

                @Override
                public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                    return delegateCallback.areItemsTheSame(oldItem, newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                    return delegateCallback.areContentsTheSame(oldItem, newItem);
                }

                @Nullable
                @Override
                public ChangePayload getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
                    return delegateCallback.getChangePayload(oldItem, newItem);
                }
            }
        );

        // noinspection ConstantConditions
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        mAdapterObserver = new AutoViewSwitcherAdapterObserver(mAdapter, mViewFlipper, mItemCount, 0, 1);
        mAdapterObserver.register();

        if (mFilterMode) {
            mEmptyView.setText(mEmptyResults);
            getViewModel()
                .filteredStream()
                .observe(this, mAdapter::submitList);

            // initial set
            submitSearchQuery("");
        } else {
            getViewModel().stream().observe(this, mAdapter::submitList);
        }
    }

    final void setEmptyViewText(String text) {
        if (!mFilterMode) {
            mEmptyView.setText(text);
        }
    }

    @OnClick(R.id.delete_fab)
    final void onDeleteSelectedItems() {

        // We could use a confirmation dialog but since it takes multiple steps to get to
        // the delete button we assume the user knows what they are doing

        final List<T> selectedItems = mAdapter.getSelectedItems();

        final Completable completable = onBeforeItemsAreDeleted(selectedItems);

        if (completable != null) {
            ProgressDialog dlg = ProgressDialog.show(
                getContext(),
                null,
                getString(R.string.removing_items),
                true,
                false
            );

            completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        disposable.dispose();
                        getViewModel().delete(selectedItems);
                        dlg.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
        } else {
            getViewModel().delete(selectedItems);
        }

        // Disable selection mode right away
        mAdapter.setInSelectionMode(false);
    }

    @Override
    public final boolean onCancelAdapterSelection() {
        if (mAdapter.isInSelectionMode()) {
            mAdapter.setInSelectionMode(false);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    final M getViewModel(Class<M> klazz) {
        return ViewModels.get(klazz, getActivity().getApplication());
    }

    public synchronized final void submitSearchQuery(String query) {
        if (!mFilterMode) {
            throw new IllegalStateException("Fragment is not in filter mode");
        }
        getViewModel().submitQuery("%" + query.replace("%", "\\%") + "%");
    }

    abstract BaseItemAdapter<T> getItemAdapter();

    abstract M getViewModel();

    abstract boolean usesAddFabButton();

    abstract Completable onBeforeItemsAreDeleted(List<T> items);
}
