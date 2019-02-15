package kr.djspi.pipe01;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kr.djspi.pipe01.fragment.MessagePopup;
import kr.djspi.pipe01.util.NfcUtil;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_DIAL;
import static android.content.Intent.ACTION_SENDTO;
import static android.text.Html.fromHtml;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BaseActivity extends AppCompatActivity {

    private static final String DJ_EMAIL = "djgis@chol.com";
    private static final String DJ_PHONE = "+82-53-424-9547";
    private DrawerLayout drawer;
    static boolean isIntentSearch = false;
    static int screenRadius;
    static Resources mRes;
    NfcUtil nfcUtil;
    Toolbar toolbar;
    Context context;
    /**
     * Tracks the status of the location updates request.
     * 앱 실행과 동시에 백그라운드에서 위치 업데이트 요청을 처리
     */
    static Boolean mRequestingLocationUpdates;
    /**
     * Represents a geographical location.
     * 앱 실행과 동시에 백그라운드에서 현재 위치를 탐색
     */
    static Location currentLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mRes = getResources();
        mRequestingLocationUpdates = false;
        requestAllPermissions();
        nfcUtil = new NfcUtil(this, getClass());
    }

    /**
     * 하위 액티비티들이 들어갈 container 와 앱공통 toolbar 구현
     * (textViewButton) '측량점 찾기' 버튼: boolean isIntentSearch = true 를 전달하여,
     * 지도 액티비티 실행과 동시에 검색창을 바로 나타내준다.
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = view.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(view);

        // 지도화면에서 사용될 기기화면 반경값을 구한다.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int screenY = displayMetrics.heightPixels;
        final int screenX = displayMetrics.widthPixels;
        screenRadius = screenY > screenX ? screenY : screenX;

        toolbar = findViewById(R.id.toolbar);
        if (useToolbar()) {
            setSupportActionBar(toolbar);
            TextView textViewButton = findViewById(R.id.nmap_find);
            textViewButton.setVisibility(VISIBLE);
            textViewButton.setOnClickListener(v -> {
                if (currentLocation == null) {
                    Toast.makeText(this, mRes.getString(R.string.error_location), Toast.LENGTH_LONG).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("isIntentSearch", true);
//                startActivity(new Intent(BaseActivity.this.getApplicationContext(), NaverMapActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                        .putExtras(bundle));
            });
        } else {
            AppBarLayout appBarLayout = findViewById(R.id.appbar);
            appBarLayout.setVisibility(GONE);
            toolbar.setVisibility(GONE);
        }
        setNavigationView(view);
    }

    /**
     * Toolbar 를 사용하지 않을 액티비티에서는 메서드를 확장해 false 를 리턴해준다.
     *
     * @return
     */
    boolean useToolbar() {
        return true;
    }

    protected void setToolbarTitle(String string) {
    }

    /**
     * 앱 사용 도움말이 표시되는 NavigationView 설정
     *
     * @param view
     */
    private void setNavigationView(@NotNull View view) {
        drawer = view.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = view.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView versionName = headerView.findViewById(R.id.versionName);
        versionName.setText(getString(R.string.nav_version_name, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE ));
        // TODO: 2019-01-31 앱 빌드시 파일명에 버전코드 붙이기 build.gradle 수정
        TextView email = headerView.findViewById(R.id.email);
        email.setText(fromHtml(getString(R.string.nav_email, DJ_EMAIL)));
        email.setOnClickListener(v ->
                startActivity(new Intent(ACTION_SENDTO, Uri.fromParts("mailto", DJ_EMAIL, null))));
        TextView phone = headerView.findViewById(R.id.phone);
        phone.setText(fromHtml(getString(R.string.nav_phone, DJ_PHONE)));
        phone.setOnClickListener(v ->
                startActivity(new Intent(ACTION_DIAL, Uri.parse("tel:" + DJ_PHONE))));
        TextView guide = headerView.findViewById(R.id.guide);
        guide.setText(fromHtml(getString(R.string.nav_guide)));
        navigationView.findViewById(R.id.nav_close).setOnClickListener(v -> drawer.closeDrawer(START));
    }

    /**
     * 앱 사용에 필요한 권한을 Array 로 입력 ('Manifest.permission.필요권한')
     * (권한 거부: onDenied) 앱 강제종료
     */
    @SuppressLint("MissingPermission")
    private void requestAllPermissions() {
        String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA};
        Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                mRequestingLocationUpdates = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                mRequestingLocationUpdates = false;
                Toast.makeText(context, "위치정보를 사용할 수 없어 앱이 종료됩니다", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (nfcUtil != null) nfcUtil.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (nfcUtil != null) nfcUtil.onPause();
    }

    /**
     * 팝업메시지 생성
     *
     * @param issue 팝업에 표시할 내용의 인식번호
     * @param tag   팝업에 표시할 내용의 인식태그
     */
    public void showMessagePopup(int issue, String tag) {
        MessagePopup messagePopup = new MessagePopup();
        Bundle bundle = new Bundle(1);
        bundle.putInt("issueType", issue);
        messagePopup.setArguments(bundle);
        messagePopup.show(getSupportFragmentManager(), tag);
    }

    public void showSnackBar(View view, String message, int showDuration, int snackBarColor) {
        Snackbar snackbar = Snackbar.make(view, message, showDuration);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(mRes.getColor(snackBarColor));
        snackbar.show();
    }
}
