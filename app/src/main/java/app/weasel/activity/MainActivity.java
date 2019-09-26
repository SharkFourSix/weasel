package app.weasel.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import app.weasel.R;
import app.weasel.adapter.FragmentAdapter;
import app.weasel.interfaces.OnAdapterSelectionChangedListener;
import app.weasel.interfaces.OnCancelAdapterSelectionListener;
import app.weasel.service.DownloadService;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class MainActivity extends BaseActivity implements OnAdapterSelectionChangedListener {

    public static final String EXTRA_FRAGMENT = "fragment";
    private static final String TAG = "MainActivity";

    // TODO Do not forget to update these when adding/removing the tabs
    public static final int FRAGMENT_INDEX_QUEUE = 0;
    public static final int FRAGMENT_INDEX_DOWNLOADS = 2;
    public static final int FRAGMENT_INDEX_PREFERENCES = 3;

    private FragmentAdapter mFragmentAdapter;

    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mViewPager.setAdapter(mFragmentAdapter = new FragmentAdapter(
                getSupportFragmentManager()
            )
        );

        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.setOffscreenPageLimit(3);

        if (!DownloadService.isServiceRunning()) {
            ContextCompat.startForegroundService(
                this,
                new Intent(this, DownloadService.class)
            );
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (mFragmentAdapter.getPreviousFragment() instanceof OnCancelAdapterSelectionListener) {
                    ((OnCancelAdapterSelectionListener) mFragmentAdapter
                        .getPreviousFragment()).onCancelAdapterSelection();
                }
            }
        });

        int i = getFragmentIndexExtra(getIntent());
        mViewPager.setCurrentItem(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return cancelSelectionIfEnabled();
    }

    @Override
    public void onBackPressed() {
        if (!cancelSelectionIfEnabled()) {
            super.onBackPressed();
        }
    }

    private boolean cancelSelectionIfEnabled() {
        final OnCancelAdapterSelectionListener l
            = (OnCancelAdapterSelectionListener) mFragmentAdapter.getCurrentFragment();
        return l != null && l.onCancelAdapterSelection();
    }

    public static int getFragmentIndexExtra(@Nullable Intent intent) {
        return intent != null
            ? intent.getIntExtra(EXTRA_FRAGMENT, FRAGMENT_INDEX_QUEUE)
            : FRAGMENT_INDEX_QUEUE;
    }

    @Override
    public void onSelectionStateChanged(boolean enabled) {
        invalidateOptionsMenu();
        setSelectionModeEnabled(enabled);
    }

    @Override
    public void onSelectionCountChanged(int selectionCount) {
        // noinspection ConstantConditions
        getSupportActionBar().setTitle(getString(R.string.selection_count, selectionCount));
    }

    @SuppressWarnings({"ConstantConditions"})
    private void setSelectionModeEnabled(boolean enabled) {
        final ActionBar actionBar = getSupportActionBar();
        if (enabled) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cross);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.app_name);
        }
    }
}
