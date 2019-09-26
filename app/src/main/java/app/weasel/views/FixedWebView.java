package app.weasel.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * <p>This extension fixes a crash on Lollipop</p>
 */
public final class FixedWebView extends WebView {
    public FixedWebView(Context context) {
        super(createConfiguration(context));
    }

    public FixedWebView(Context context, AttributeSet attrs) {
        super(createConfiguration(context), attrs);
    }

    public FixedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(createConfiguration(context), attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FixedWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(createConfiguration(context), attrs, defStyleAttr, defStyleRes);
    }

    public FixedWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(createConfiguration(context), attrs, defStyleAttr, privateBrowsing);
    }

    private static Context createConfiguration(Context context) {
        return context.createConfigurationContext(new Configuration());
    }
}
