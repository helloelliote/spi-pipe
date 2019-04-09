package kr.djspi.pipe01;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.button.MaterialButton;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.helloelliote.geolocation.GeoPoint;
import com.helloelliote.geolocation.GeoTrans;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMap.MapType;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;

import java.io.Serializable;
import java.util.Locale;

import kr.djspi.pipe01.fragment.LocationDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.SurveyDialog;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.helloelliote.geolocation.GeoTrans.Coordinate.GEO;
import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;
import static com.naver.maps.map.LocationTrackingMode.None;
import static com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING;
import static com.naver.maps.map.util.MapConstants.EXTENT_KOREA;
import static kr.djspi.pipe01.BuildConfig.NAVER_CLIENT_ID;
import static kr.djspi.pipe01.Const.RESULT_FAIL;
import static kr.djspi.pipe01.Const.TAG_LOCATION;
import static kr.djspi.pipe01.Const.TAG_SURVEY;
import static kr.djspi.pipe01.fragment.SurveyDialog.originPoint;

public class SpiLocationActivity extends LocationUpdate implements OnMapReadyCallback, OnClickListener, OnSelectListener, Serializable {

    private static final double ZOOM_DEFAULT = 19.0; // 기본 줌레벨
    private static final double ZOOM_MIN = 12.0; // 최소 줌레벨
    private static final double ZOOM_MAX = NaverMap.MAXIMUM_ZOOM; // 최대 줌레벨(21)
    private boolean isSelected = true;
    private LocationDialog selectDialog = new LocationDialog();
    private TextView textView;
    private MaterialButton buttonConfirm;
    private NaverMap naverMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // https://console.ncloud.com/mc/solution/naverService/application 에서 클라이언트 ID 발급
        NaverMapSdk.getInstance(this)
                .setClient(new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));
        setContentView(R.layout.activity_spi_location);
        setNaverMap();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findViewById(R.id.nmap_find).setVisibility(GONE); // '측량점 찾기' 버튼 없앰
        textView = findViewById(R.id.record_gps);
        textView.setOnClickListener(this);
        buttonConfirm = findViewById(R.id.btn_confirm);
        buttonConfirm.setOnClickListener(this);

        setToolbarTitle(getString(R.string.record_location_title));
    }

    @Override
    void setToolbarTitle(String string) {
        toolbar.setTitle(string);
    }

    /**
     * 네이버 지도의 기초 UI 설정
     */
    @UiThread
    private void setNaverMap() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(new NaverMapOptions()
                    .camera(new CameraPosition(new LatLng(currentLocation), ZOOM_DEFAULT, 0, 0))
                    .enabledLayerGroups(LAYER_GROUP_BUILDING)
                    .locale(Locale.KOREA)
                    .minZoom(ZOOM_MIN)
                    .maxZoom(ZOOM_MAX)
                    .extent(EXTENT_KOREA)
                    .compassEnabled(true)
                    .locationButtonEnabled(true)
                    .zoomGesturesEnabled(true)
            );
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * 네이버 지도의 기초 동작 설정. SpiLocationActivity 객체가 준비되면 onMapReady() 콜백 메서드가 호출
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
     *                 getMapAsync() 메서드로 OnMapReadyCallback 을 등록하면 SpiLocationActivity 객체를 얻는다.
     * @see SpiLocationActivity#setMapModeSwitch(NaverMap)
     */
    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(None);
        naverMap.addOnCameraChangeListener((reason, animated) -> {
            if (reason == REASON_GESTURE) textView.setAlpha(0.25f);
        });
        setMapModeSwitch(naverMap);

        runOnUiThread(() -> {
            selectDialog.setCancelable(false);
            selectDialog.show(getSupportFragmentManager(), TAG_LOCATION);
        });
    }

    /**
     * Toolbar 에서 지도 모드 전환 스위치 구현
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 SpiLocationActivity 객체
     */
    private void setMapModeSwitch(@NonNull NaverMap naverMap) {
        ToggleSwitch toggleSwitch = findViewById(R.id.nmap_mapmode_switch);
        toggleSwitch.setVisibility(VISIBLE);
        toggleSwitch.setCheckedPosition(0);
        toggleSwitch.setOnChangeListener((int position) -> {
            switch (position) {
                case 0:
                    naverMap.setMapType(MapType.Basic);
                    break;
                case 1:
                    naverMap.setMapType(MapType.Hybrid);
                    break;
                default:
                    naverMap.setMapType(MapType.Basic);
                    break;
            }
        });
    }

    @Override
    public void onLocationUpdate(Location location) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_gps:
                v.setAlpha(1.0f);
                break;
            case R.id.btn_confirm:
                try {
                    final LatLng latLng = naverMap.getCameraPosition().target;
                    final double[] spiLocation = {latLng.latitude, latLng.longitude};
                    setResult(RESULT_OK, new Intent().putExtra("locations", spiLocation));
                    finish();
                } catch (Exception e) {
                    showMessageDialog(0, getString(R.string.toast_error_location), true);
                    return;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelect(String tag, int index, @Nullable String... text) {
        switch (tag) {
            case TAG_LOCATION:
                if (index == RESULT_FAIL) {
                    isSelected = false;
                    onBackPressed();
                    return;
                } else if (index == 1) {
                    SurveyDialog surveyDialog = new SurveyDialog();
                    surveyDialog.setCancelable(false);
                    surveyDialog.show(getSupportFragmentManager(), TAG_SURVEY);
                    onPause();
                } else {
                    textView.setVisibility(VISIBLE);
                    buttonConfirm.setVisibility(VISIBLE);
                }
                break;
            case TAG_SURVEY:
                if (index == RESULT_FAIL) {
                    isSelected = false;
                    onBackPressed();
                    return;
                }
                assert text != null;
                LatLng surveyLatLng = convertTmToLatLng(Double.valueOf(text[0]), Double.valueOf(text[1]));
                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(surveyLatLng, ZOOM_DEFAULT).animate(CameraAnimation.Fly);
                naverMap.moveCamera(cameraUpdate);
                buttonConfirm.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * Tm 좌표계를 LatLng 경위도 좌표계로 변환한다. 반드시 변환 과정에서 X, Y 좌표를 반전시켜 리턴한다.
     *
     * @param Tm1
     * @param Tm2
     * @return
     */
    @NonNull
    private LatLng convertTmToLatLng(double Tm1, double Tm2) {
        GeoPoint surveyPoint = GeoTrans.convert(originPoint, GEO, new GeoPoint(Tm2, Tm1));
        return new LatLng(surveyPoint.getY(), surveyPoint.getX());
    }

    @Override
    public void onBackPressed() {
        if (selectDialog.isAdded() && isSelected) return;
        textView.setVisibility(INVISIBLE);
        buttonConfirm.setVisibility(INVISIBLE);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        originPoint = null;
        selectDialog = null;
    }
}
