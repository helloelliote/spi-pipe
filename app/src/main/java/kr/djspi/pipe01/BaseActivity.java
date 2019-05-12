package kr.djspi.pipe01;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SuperviseGet;
import com.naver.maps.map.NaverMapSdk;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.fragment.MessageDialog;
import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.sql.Supervise;
import kr.djspi.pipe01.sql.SuperviseDao;
import kr.djspi.pipe01.sql.SuperviseDatabase;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.URL_SPI;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static Resources resources;
    public static String packageName;
    public static float screenScale;
    private DrawerLayout drawer;
    public SuperviseDatabase superviseDb;
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
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenScale = (float) displayMetrics.widthPixels / 1440.0f;
        if (superviseDb == null) {
            superviseDb = Room.databaseBuilder(getApplicationContext(), SuperviseDatabase.class, "db_supervise").build();
        }
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
    protected boolean useToolbar() {
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

    void setToolbar(String title) {
    }

    /**
     * 연락처, 버전 정보, 앱 사용 도움말 등이 표시되는 NavigationView 설정
     */
    private void setNavigationView(@NotNull View view) {
        drawer = view.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        toggle.setToolbarNavigationClickListener(v -> drawer.openDrawer(GravityCompat.START));
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
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
            case R.id.nav_supervise:
                updateLocalSuperviseDatabase();
                break;
            case R.id.nav_clear_cache:
                NaverMapSdk.getInstance(this).flushCache(() ->
                        Toast.makeText(BaseActivity.this, "캐시를 초기화하였습니다.", Toast.LENGTH_SHORT).show());
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

    private void updateLocalPipeTypeDatabase() {

    }

    private void updateLocalSuperviseDatabase() {
        JsonObject jsonQuery = new JsonObject();
        jsonQuery.addProperty("json", "");
        Retrofit2x.builder()
                .setService(new SuperviseGet(URL_SPI))
                .setQuery(jsonQuery).build()
                .run(new OnRetrofitListener() {

                    @Override
                    public void onResponse(JsonObject response) {
                        new Thread(() -> {
                            final JsonArray jsonArray = Json.a(response, "data");
                            SuperviseDao superviseDao = superviseDb.dao();
                            for (JsonElement element : jsonArray) {
                                JsonObject object = element.getAsJsonObject();
                                superviseDao.insert(new Supervise(Json.i(object, "id"), Json.s(object, "supervise")));
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });
        Toast.makeText(this, "데이터베이스를 갱신하였습니다.", Toast.LENGTH_SHORT).show();
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
