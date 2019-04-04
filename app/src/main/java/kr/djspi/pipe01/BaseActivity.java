package kr.djspi.pipe01;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.fragment.MessageDialog;
import kr.djspi.pipe01.nfc.NfcUtil;

import static android.content.Intent.ACTION_DIAL;
import static android.content.Intent.ACTION_SENDTO;
import static android.text.Html.fromHtml;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.BuildConfig.BUILD_TYPE;
import static kr.djspi.pipe01.BuildConfig.VERSION_NAME;

public class BaseActivity extends AppCompatActivity {

    // TODO: 2019-03-22 통신이 끊어진 상태에서 앱으로 태깅했을 때 내부 데이터를 보여주는 모듈을 추가
    public static Resources resources;
    public static String packageName;
    private DrawerLayout drawer;
    static Location currentLocation; // 앱 실행과 동시에 백그라운드에서 현재 위치를 탐색
    NfcUtil nfcUtil;
    ProgressBar progressBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        nfcUtil = new NfcUtil(this, getClass());
        packageName = getPackageName();
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = view.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(view);

        toolbar = findViewById(R.id.toolbar);
        if (useToolbar()) setSupportActionBar(toolbar);
        else toolbar.setVisibility(GONE);
        setNavigationView(view);
        progressBar = findViewById(R.id.progressbar);
    }

    /**
     * @return Toolbar 를 사용하지 않을 액티비티에서는 오버라이딩해 false 를 리턴
     */
    boolean useToolbar() {
        return true;
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.TitleHeader);
        TextView textViewButton = findViewById(R.id.nmap_find);
        textViewButton.setVisibility(VISIBLE);
        textViewButton.setOnClickListener(v -> {
            if (currentLocation != null) {
                startActivity(new Intent(this, NaverMapActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } else {
                Toast.makeText(this, getString(R.string.toast_error_location), Toast.LENGTH_LONG).show();
            }
        });
    }

    void setToolbarTitle(String title) {
    }

    /**
     * 연락처, 버전 정보, 앱 사용 도움말 등이 표시되는 NavigationView 설정
     */
    private void setNavigationView(@NotNull View view) {
        drawer = view.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = view.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView versionName = headerView.findViewById(R.id.versionName);
        versionName.setText(getString(R.string.nav_version_name, VERSION_NAME, BUILD_TYPE));

        TextView email = headerView.findViewById(R.id.email);
        String emailStr = getString(R.string.nav_dj_email);
        email.setText(fromHtml(getString(R.string.nav_email, emailStr)));
        email.setOnClickListener(v ->
                startActivity(new Intent(ACTION_SENDTO, Uri.fromParts("mailto", emailStr, null))));

        TextView phone = headerView.findViewById(R.id.phone);
        String phoneStr = getString(R.string.nav_dj_phone);
        phone.setText(fromHtml(getString(R.string.nav_phone, phoneStr)));
        phone.setOnClickListener(v ->
                startActivity(new Intent(ACTION_DIAL, Uri.parse("tel:" + phoneStr))));

        TextView guide = headerView.findViewById(R.id.guide);
        guide.setText(fromHtml(getString(R.string.nav_guide)));
        navigationView.findViewById(R.id.nav_close).setOnClickListener(v -> drawer.closeDrawer(START));
        navigationView.findViewById(R.id.nav_swipe_close).setOnClickListener(v -> drawer.closeDrawer(START));
    }

    /**
     * 팝업 다이얼로그 생성
     *
     * @param issue 팝업에 표시할 내용의 인식번호
     * @param tag   팝업에 표시할 내용의 인식태그
     */
    void showMessageDialog(int issue, String tag, boolean isCancelable) {
        try {
            MessageDialog dialog = new MessageDialog();
            dialog.setCancelable(isCancelable);
            Bundle bundle = new Bundle(1);
            bundle.putInt("issueType", issue);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), tag);
        } catch (IllegalStateException ignore) {
        }
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcUtil != null) nfcUtil.onPause();
    }
}
