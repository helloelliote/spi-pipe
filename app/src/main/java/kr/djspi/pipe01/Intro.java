package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public final class Intro extends AppCompatActivity {

    private static final boolean isDelayed = false;
    private static final int delayMillis = 2000; // Intro 화면 표시시간

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

        if (isDelayed) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }, delayMillis);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("state", "launch");
            startActivity(intent);
            finish();
        }
    }
}
