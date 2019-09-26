package app.weasel.adapter;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import app.weasel.fragment.DownloadsFragment;
import app.weasel.fragment.ItemFragment;
import app.weasel.fragment.LibraryFragment;
import app.weasel.fragment.PreferencesFragment;
import app.weasel.fragment.QueueFragment;

public class FragmentAdapter extends FragmentPagerAdapter {

    private Fragment mCurrentFragment;
    private Fragment mPreviousFragment;

    private final String[] mFragmentClasses = {
        QueueFragment.class.getName(),
        LibraryFragment.class.getName(),
        DownloadsFragment.class.getName(),
        PreferencesFragment.class.getName()
    };

    private final String[] mFragmentTagTitles = {
        "Queue",
        "Library",
        "Downloads",
        "Preferences"
    };

    private final boolean[][] mArguments = {
        // showAddButton, hasOptionsMenu
        {true, true},
        {false, true},
        {false, true},
        {false, false}
    };

    private final FragmentManager mFragmentManager;

    public FragmentAdapter(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mFragmentManager = fm;
    }

    @NonNull
    @Override
    @SuppressWarnings({"ConstantConditions"})
    public Fragment getItem(int position) {
        Fragment fragment = mFragmentManager
            .getFragmentFactory()
            .instantiate(getClass().getClassLoader(), mFragmentClasses[position]);
        final Bundle extras = new Bundle();

        extras.putBoolean(QueueFragment.EXTRA_SHOW_ADD_BUTTON, mArguments[position][0]);
        extras.putBoolean(ItemFragment.EXTRA_SHOW_SEARCH_MENU, mArguments[position][1]);

        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mPreviousFragment = mCurrentFragment;
        mCurrentFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTagTitles[position];
    }

    @Override
    public int getCount() {
        return mFragmentClasses.length;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public Fragment getPreviousFragment() {
        return mPreviousFragment;
    }
}
