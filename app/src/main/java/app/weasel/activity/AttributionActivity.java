package app.weasel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import app.weasel.R;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class AttributionActivity extends BaseActivity {

    @BindView(R.id.listView)
    ListView mListView;

    @BindArray(R.array.library_list)
    String[] mLibraryList;

    @BindArray(R.array.library_licences)
    String[] mLibraryLicenses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribution);

        ButterKnife.bind(this);

        mListView.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            mLibraryList
        ));

        mListView.setOnItemClickListener((adapterView, view, position, id) -> startActivity(
            new Intent(this, AttributionLicenseActivity.class)
                .putExtra(
                    "library",
                    mLibraryList[position]
                ).putExtra(
                "license",
                mLibraryLicenses[position]
            )
        ));
    }
}
