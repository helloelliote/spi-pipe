package kr.djspi.pipe01;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import kr.djspi.pipe01.fragment.MessageDialog;
import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.sql.SuperviseDatabase;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BaseActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    public static Resources resources;
    public static String packageName;
    public static float screenScale;
    public static SuperviseDatabase superviseDb;
    static Location currentLocation; // 앱 실행과 동시에 백그라운드에서 현재 위치를 탐색
    private DrawerLayout drawer;
    Toolbar toolbar;
    ProgressBar progressBar;
    NfcUtil nfcUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        nfcUtil = new NfcUtil(this, getClass());
        packageName = getPackageName();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenScale = (float) displayMetrics.widthPixels / 1440.0f;
        new Thread(() -> {
            if (superviseDb == null) {
                superviseDb = Room.databaseBuilder(getApplicationContext(), SuperviseDatabase.class, "db_supervise").build();
            }
        }).start();
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = view.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(view);

        toolbar = view.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = view.findViewById(R.id.drawer_layout);
        progressBar = view.findViewById(R.id.progressbar);

        new Thread(() -> {
            if (useToolbar()) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        BaseActivity.this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
                toggle.setDrawerIndicatorEnabled(false);
                toggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
                toggle.setToolbarNavigationClickListener(v -> drawer.openDrawer(GravityCompat.START));
                drawer.addDrawerListener(toggle);
                toggle.syncState();

                NavigationView navigationView = drawer.findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                Object what = new TextAppearanceSpan((this), R.style.TextAppearance20sp);

                MenuItem titleInit = menu.findItem(R.id.title_app);
                SpannableString spannable = new SpannableString(titleInit.getTitle());
                spannable.setSpan(what, 0, spannable.length(), 0);
                titleInit.setTitle(spannable);

                MenuItem titleNfc = menu.findItem(R.id.title_setting);
                spannable = new SpannableString(titleNfc.getTitle());
                spannable.setSpan(what, 0, spannable.length(), 0);
                titleNfc.setTitle(spannable);

                View headerView = navigationView.getHeaderView(0);
                navigationView.setNavigationItemSelectedListener(BaseActivity.this);

                TextView versionName = headerView.findViewById(R.id.versionName);
                versionName.setText(getString(R.string.nav_version_name, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE));

                TextView email = headerView.findViewById(R.id.email);
                String emailStr = getString(R.string.nav_dj_email);
                email.setText(getString(R.string.nav_email, emailStr));
                email.setOnClickListener(v ->
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailStr, null))));

                TextView phone = headerView.findViewById(R.id.phone);
                String phoneStr = getString(R.string.nav_dj_phone);
                phone.setText(phoneStr);
                phone.setOnClickListener(v ->
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneStr))));

                navigationView.findViewById(R.id.nav_close).setOnClickListener(v -> drawer.closeDrawer(GravityCompat.START));
            } else {
                toolbar.setVisibility(GONE);
            }
        }).start();
    }

    /**
     * @return Toolbar 를 사용하지 않을 액티비티에서는 오버라이딩해 false 를 리턴
     */
    boolean useToolbar() {
        return true;
    }

    void setToolbar(String title) {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        drawer.closeDrawer(GravityCompat.START, false);

        int menuId = menuItem.getItemId();

        switch (menuId) {
            case R.id.nav_guide:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog alertDialog = builder
                        .setTitle("SPI 정보")
                        .setMessage(getString(R.string.nav_guide_content))
                        .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss()).create();
                alertDialog.show();
                TextView textView = alertDialog.findViewById(android.R.id.message);
                if (textView != null) {
                    textView.setTextSize(14.0f);
                }
                break;
            case R.id.nav_homepage:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.nav_dj_homepage))));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcUtil != null) nfcUtil.onPause();
    }
}
