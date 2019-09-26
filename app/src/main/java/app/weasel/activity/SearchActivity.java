package app.weasel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import app.weasel.R;
import app.weasel.fragment.ItemFragment;
import app.weasel.fragment.QueueFragment;
import app.weasel.interfaces.OnAdapterSelectionChangedListener;
import app.weasel.interfaces.OnCancelAdapterSelectionListener;
import app.weasel.model.Item;
import app.weasel.persistence.viewmodel.BaseViewModel;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public final class SearchActivity extends BaseActivity
    implements OnAdapterSelectionChangedListener {

    private final static String EXTRA_FRAGMENT_NAME = "class";
    private final static String EXTRA_FRAGMENT_ARGS = "args";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();

        // noinspection ConstantConditions
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_filter);

        ButterKnife.bind(this, actionBar.getCustomView());

        final String fragmentClass = getIntent().getStringExtra(EXTRA_FRAGMENT_NAME);
        final Bundle arguments = getIntent().getBundleExtra(EXTRA_FRAGMENT_ARGS);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Fragment fragment = fragmentManager
            .getFragmentFactory()
            .instantiate(getClassLoader(), fragmentClass);

        fragment.setArguments(arguments);

        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit();
    }

    @OnClick(R.id.close_search)
    void onCloseButtonClicked() {
        finish();
    }

    @OnTextChanged(R.id.input)
    void onSearchTextChanged(CharSequence text) {
        ((ItemFragment) getSupportFragmentManager()
            .getFragments()
            .get(0)
        ).submitSearchQuery(text.toString());
    }

    public static <T extends Item, M extends BaseViewModel<T>, F extends ItemFragment<T, M>>
    void startActivity(@NonNull Context context, @NonNull Class<F> klazz) {
        final Bundle extras = new Bundle();

        extras.putBoolean(ItemFragment.EXTRA_SHOW_SEARCH_MENU, false);
        extras.putBoolean(ItemFragment.EXTRA_FILTER_MODE, true);
        if (QueueFragment.class == klazz) {
            extras.putBoolean(QueueFragment.EXTRA_SHOW_ADD_BUTTON, false);
        }
        context.startActivity(
            new Intent(
                context,
                SearchActivity.class
            ).putExtra(
                EXTRA_FRAGMENT_NAME,
                klazz.getName()
            ).putExtra(
                EXTRA_FRAGMENT_ARGS,
                extras
            )
        );
    }

    @Override
    public void onBackPressed() {
        if (mInSelectionMode) {
            ((OnCancelAdapterSelectionListener) getSupportFragmentManager().getFragments().get(0)).onCancelAdapterSelection();
        } else {
            super.onBackPressed();
        }
    }

    private boolean mInSelectionMode = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSelectionStateChanged(boolean enabled) {
        mInSelectionMode = enabled;
    }

    @Override
    public void onSelectionCountChanged(int selectionCount) {
    }
}
