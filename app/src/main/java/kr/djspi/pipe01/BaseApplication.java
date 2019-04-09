package kr.djspi.pipe01;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class BaseApplication extends Application {

    private static final boolean USE_LEAK_CANARY = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (USE_LEAK_CANARY) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
            // Normal app init code...
        }
    }
}
