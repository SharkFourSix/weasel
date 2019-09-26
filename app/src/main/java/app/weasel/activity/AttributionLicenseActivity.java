package app.weasel.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import app.weasel.R;
import app.weasel.views.FixedWebView;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class AttributionLicenseActivity extends BaseActivity {

    @BindView(R.id.webView)
    FixedWebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_license_viewer);

        ButterKnife.bind(this);

        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        setTitle(getIntent().getStringExtra("library"));

        mWebView.loadData(
            getIntent().getStringExtra("license"),
            "text/html",
            "UTF-8"
        );
    }
}
