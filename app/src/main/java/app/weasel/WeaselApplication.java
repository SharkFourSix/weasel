package app.weasel;

import android.app.Application;

import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import app.weasel.util.PreferenceUtils;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public final class WeaselApplication extends Application {

    public static WeaselApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(
                new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build());

        getResources()
            .getConfiguration()
            .setLocale(new Locale(PreferenceUtils.getLanguage(this)));

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        INSTANCE = this;
    }

    private static WeaselApplication INSTANCE;
}
