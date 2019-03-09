package kr.djspi.pipe01;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.fragment.MessageDialog;

import static android.content.Intent.ACTION_DIAL;
import static android.content.Intent.ACTION_SENDTO;
import static android.text.Html.fromHtml;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.BuildConfig.BUILD_TYPE;
import static kr.djspi.pipe01.BuildConfig.VERSION_NAME;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    static Resources resources;
    static Location currentLocation; // 앱 실행과 동시에 백그라운드에서 현재 위치를 탐색
    Context context;
    Toolbar toolbar;
    DrawerLayout drawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        resources = getResources();
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = view.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(view);

        toolbar = findViewById(R.id.toolbar);
        if (useToolbar()) setSupportActionBar(toolbar);
        else findViewById(R.id.appbar).setVisibility(GONE);

        if (useNavigationView()) setNavigationView(view, true);
        else setNavigationView(view, false);
    }

    /**
     * @return Toolbar 를 사용하지 않을 액티비티에서는 오버라이딩해 false 를 리턴
     */
    boolean useToolbar() {
        return true;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        TextView textViewButton = findViewById(R.id.nmap_find);
        textViewButton.setVisibility(VISIBLE);
        textViewButton.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(this, getString(R.string.toast_error_location), Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(context, NaverMapActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });
    }

    protected void setToolbarTitle(String string) {
    }

    /**
     * @return Drawer 를 사용하지 않을 액티비티에서는 오버라이딩해 false 를 리턴
     */
    boolean useNavigationView() {
        return true;
    }

    /**
     * 연락처, 버전 정보, 앱 사용 도움말 등이 표시되는 NavigationView 설정
     */
    void setNavigationView(@NotNull View view, boolean useHeader) {
        drawer = view.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (useHeader) {
            NavigationView navigationView = view.findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            TextView versionName = headerView.findViewById(R.id.versionName);
            versionName.setText(getString(R.string.nav_version_name, VERSION_NAME, BUILD_TYPE));
            // TODO: 2019-01-31 앱 빌드시 파일명에 버전코드 붙이기 build.gradle 수정
            TextView email = headerView.findViewById(R.id.email);
            email.setText(fromHtml(getString(R.string.nav_email, "djspi@chol.com")));
            email.setOnClickListener(v ->
                    startActivity(new Intent(ACTION_SENDTO, Uri.fromParts("mailto", "djspi@chol.com", null))));
            TextView phone = headerView.findViewById(R.id.phone);
            phone.setText(fromHtml(getString(R.string.nav_phone, "+82-53-424-9547")));
            phone.setOnClickListener(v ->
                    startActivity(new Intent(ACTION_DIAL, Uri.parse("tel:" + "+82-53-424-9547"))));
            TextView guide = headerView.findViewById(R.id.guide);
            guide.setText(fromHtml(getString(R.string.nav_guide)));
            navigationView.findViewById(R.id.nav_close).setOnClickListener(v -> drawer.closeDrawer(START));
        }
    }

    /**
     * 팝업 다이얼로그 생성
     *
     * @param issue 팝업에 표시할 내용의 인식번호
     * @param tag   팝업에 표시할 내용의 인식태그
     */
    public void showMessagePopup(int issue, String tag) {
        MessageDialog dialog = new MessageDialog();
        Bundle bundle = new Bundle(1);
        bundle.putInt("issueType", issue);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
