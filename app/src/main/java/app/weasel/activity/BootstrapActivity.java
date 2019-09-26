package app.weasel.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import app.weasel.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BootstrapActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_ID = 100;

    @BindView(R.id.linearLayout)
    LinearLayout mLinearLayout;

    private int mTargetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootstrap);
        ButterKnife.bind(this);

        // Cache the fragment index to be forwarded to the main activity if present
        mTargetFragment = MainActivity.getFragmentIndexExtra(getIntent());

        if (hasWritePermission()) {
            proceedToMainActivity();
        } else {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_ID
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasWritePermission()) {
            proceedToMainActivity();
        }
    }

    @OnClick(R.id.preferences)
    void openApplicationPreferences() {
        startActivity(
            new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).setData(
                Uri.fromParts(
                    "package",
                    getPackageName(),
                    null
                )
            )
        );
    }

    private void proceedToMainActivity() {
        startActivity(
            new Intent(
                this,
                MainActivity.class
            ).putExtra(
                MainActivity.EXTRA_FRAGMENT,
                mTargetFragment
            )
        );
        finish();
    }

    private boolean hasWritePermission() {
        return ActivityCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMainActivity();
            } else {
                mLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }
}
