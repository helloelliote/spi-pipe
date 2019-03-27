package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public final class Intro extends AppCompatActivity {

    private static final int delayMillis = 750; // Intro 화면 표시시간

    /**
     * (isDelayed) 지정 시간 후 전환되는 스플래시 화면
     * (!isDelayed) 앱 로딩이 끝나면 바로 전환되는 스플래시 화면
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.BUILD_TYPE.equals("release")) {
            final String installer = this.getPackageManager().getInstallerPackageName(this.getPackageName());
            if (!installer.startsWith("com.android.vending")) {
                finishAffinity();
                System.runFinalization();
                System.exit(0);
                finish();
            }
        }

        new Handler().postDelayed(() -> {
            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();
        }, delayMillis);
    }
}
