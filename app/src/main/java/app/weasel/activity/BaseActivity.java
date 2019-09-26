package app.weasel.activity;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import app.weasel.event.EventPublisher;
import app.weasel.event.ExitApplicationEvent;
import app.weasel.util.PlatformUtils;
import app.weasel.util.PreferenceUtils;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlatformUtils.setNightMode(PreferenceUtils.isNightModeEnabled(this));
        EventPublisher.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventPublisher.unregister(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply custom font using Calligraphy
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Subscribe
    public void exitApplication(ExitApplicationEvent event) {
        finishAffinity();
    }
}
