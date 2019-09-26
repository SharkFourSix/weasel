package app.weasel.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.weasel.R;
import app.weasel.event.NewQueueItemEvent;
import app.weasel.event.SongDiscoveryEvent;
import app.weasel.views.FixedWebView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class BrowserActivity extends BaseActivity {

    private static final String TAG = "BrowserFragment";

    @BindView(R.id.webView)
    FixedWebView mWebView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.fab)
    FloatingActionButton mCancelLoadingFab;

    @BindView(R.id.statusText)
    AppCompatTextView mStatusText;

    @BindView(R.id.statusView)
    View mStatusView;

    private final Gson mGson = new Gson();

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_browser);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                mCancelLoadingFab.show();
                mWebView.reload();
            }
        );
        mHandler = new Handler();
        setupWebView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            // Because parent is/(might be) already registered
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.fab)
    void cancelWebViewLoading() {
        mWebView.stopLoading();
    }

    private void setupWebView() {
        final WebSettings webSettings = mWebView.getSettings();

        webSettings.setBuiltInZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        mWebView.setBackgroundColor(Color.TRANSPARENT);

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mCancelLoadingFab.hide();
                } else {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mCancelLoadingFab.show();
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            // We could have used mWebView.addJavascriptInterface(); but that poses security threats
            // to the app and device
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v(TAG, "onPageFinished");

                String fn = "(" +
                    "function(){" +
                    "var c = document.getElementsByTagName('a');" +
                    "var ids=[];" +
                    "var cb=function(arr,v){ if(v != null && arr.indexOf(v)==-1){arr.push(v); return true;} return false;};" +
                    "for(var i=0; i<c.length; i++){" +
                    "var x=/(song\\=([0-9]+))|(song\\.php\\?id\\=([0-9]+))|([A-Z]\\/[0-9]+-.+\\/[0-9]+-.+\\/([0-9]+)-.+$)/.exec(c[i].href);" +
                    "if(x != null){" +
                    "if(!cb(ids, x[2])) if(!cb(ids, x[4])) cb(ids, x[6]);" +
                    "}" +
                    "}" +
                    "return ids;" +
                    "}" +
                    ")()";

                view.evaluateJavascript(fn, result -> {
                    if (result != null) {
                        result = result.replace("\"", "");
                        final long[] idList = mGson.fromJson(result, long[].class);
                        if (idList.length > 0) {
                            EventBus.getDefault().post(new NewQueueItemEvent(idList));
                        }
                        Log.v(TAG, "Found " + idList.length + " ids");
                    }
                });
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.v(TAG, "onPageStarted");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (filterResource(url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (filterResource(request.getUrl().toString())) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        mWebView.loadUrl("http://m.malawi-music.com/");
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onSongDiscovered(SongDiscoveryEvent event) {
        switch (event.getEventCode()) {
            case song_discovered:
                mHandler.removeCallbacks(mStatusViewDismisser);
                mStatusView.setVisibility(View.VISIBLE);
                mStatusText.setText(event.getSongTitle());
                mHandler.postDelayed(mStatusViewDismisser, TimeUnit.SECONDS.toMillis(5));
                break;
        }
    }

    private final Runnable mStatusViewDismisser = new Runnable() {
        @Override
        public void run() {
            mStatusView.setVisibility(View.GONE);
        }
    };

    private boolean filterResource(String url) {
        // TODO block annoying advertisement assets here (EasyList)
        return false;
    }
}
