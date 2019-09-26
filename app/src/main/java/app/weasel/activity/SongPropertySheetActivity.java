package app.weasel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import app.weasel.R;
import app.weasel.databinding.ActivityPropertySheetBinding;
import app.weasel.model.LibraryItem;
import app.weasel.util.PlatformUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class SongPropertySheetActivity extends BaseActivity {

    private static final String EXTRA_ITEM = "item";

    @BindView(R.id.album_art)
    AppCompatImageView albumArt;

    @BindView(R.id.toolbar)
    Toolbar mToolBar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private LibraryItem mLibraryItem;

    @Override
    @SuppressWarnings({"ConstantConditions"})
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPropertySheetBinding binding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_property_sheet,
            null,
            false
        );

        setContentView(binding.getRoot());

        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);

        mLibraryItem = (LibraryItem) getIntent().getSerializableExtra(EXTRA_ITEM);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        mCollapsingToolbarLayout.setTitle(mLibraryItem.getArtist());

        binding.setSong(mLibraryItem);
    }

    public static void viewItem(Context context, LibraryItem item) {
        context.startActivity(
            new Intent(
                context,
                SongPropertySheetActivity.class
            ).putExtra(
                EXTRA_ITEM,
                item
            )
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.play_button)
    void openMusicPlayer() {
        PlatformUtils.selectPlayer(this, mLibraryItem.getFilePath());
    }
}
