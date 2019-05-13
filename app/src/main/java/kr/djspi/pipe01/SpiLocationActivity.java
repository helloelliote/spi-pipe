package kr.djspi.pipe01;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.geolocation.GeoPoint;
import com.helloelliote.util.geolocation.GeoTrans;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore;
import com.helloelliote.util.retrofit.SpiGet;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMap.MapType;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.io.Serializable;
import java.util.Locale;

import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.SurveyDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.helloelliote.util.geolocation.GeoTrans.Coordinate.GEO;
import static com.helloelliote.util.retrofit.ApiKey.API_PIPE_GET;
import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;
import static com.naver.maps.map.LocationTrackingMode.None;
import static com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING;
import static com.naver.maps.map.overlay.OverlayImage.fromResource;
import static com.naver.maps.map.util.MapConstants.EXTENT_KOREA;
import static kr.djspi.pipe01.BuildConfig.NAVER_CLIENT_ID;
import static kr.djspi.pipe01.Const.RESULT_PASS;
import static kr.djspi.pipe01.Const.TAG_SURVEY;
import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.dto.PipeType.parsePipeType;
import static kr.djspi.pipe01.fragment.SurveyDialog.originPoint;

public class SpiLocationActivity extends LocationUpdate implements OnMapReadyCallback, OnClickListener, OnSelectListener, Serializable {

    private static final double ZOOM_DEFAULT = 20.0; // 기본 줌레벨
    private static final double ZOOM_MIN = 12.0; // 최소 줌레벨
    private static final double ZOOM_GET = 12.0;
    private static final double ZOOM_MAX = NaverMap.MAXIMUM_ZOOM; // 최대 줌레벨(21)
    private boolean isSelected = true;
    private TextView textView;
    private NaverMap naverMap;
    private SurveyDialog surveyDialog = new SurveyDialog();

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
        textView = findViewById(R.id.record_gps);
        textView.setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);

        setToolbar(getString(R.string.record_location_title));
    }

    @Override
    void setToolbar(String string) {
        toolbar.setTitle(string);
        toolbar.findViewById(R.id.nmap_find).setVisibility(GONE); // '측량점 찾기' 버튼 없앰
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

        SharedPreferences defPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean useSurvey = defPreferences.getBoolean("switch_location", false);
        if (useSurvey) {
            runOnUiThread(() -> {
                textView.setVisibility(View.INVISIBLE);
                surveyDialog.setCancelable(false);
                surveyDialog.show(getSupportFragmentManager(), TAG_SURVEY);
            });
        }

        new Thread(() -> onRequestPipe(naverMap)).start();
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

    private void onRequestPipe(@NonNull NaverMap naverMap) {
        JsonObject jsonQuery = new JsonObject();
        final LatLngBounds bounds = naverMap.getContentBounds();
        jsonQuery.addProperty("sx", String.valueOf(bounds.getWestLongitude()));
        jsonQuery.addProperty("sy", String.valueOf(bounds.getSouthLatitude()));
        jsonQuery.addProperty("nx", String.valueOf(bounds.getEastLongitude()));
        jsonQuery.addProperty("ny", String.valueOf(bounds.getNorthLatitude()));

        Retrofit2x.builder()
                .setService(new SpiGet(URL_SPI, API_PIPE_GET))
                .setQuery(jsonQuery).build()
                .run(new RetrofitCore.OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        if (Json.i(response, "total_count") != 0) {
                            JsonArray jsonArray = Json.a(response, "data");
                            for (JsonElement element : jsonArray) {
                                JsonObject jsonObject = element.getAsJsonObject();
                                setMarker(jsonObject);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
//                        showMessageDialog(8, throwable.getMessage(), true);
                    }

                    private void setMarker(@NonNull JsonObject jsonObject) {
                        double lat = Json.d(jsonObject, "spi_latitude");
                        double lng = Json.d(jsonObject, "spi_longitude");
                        int resId = parsePipeType(Json.s(jsonObject, "pipe")).getDrawRes();
                        Marker marker = new Marker(new LatLng(lat, lng), fromResource(resId));
                        marker.setMinZoom(ZOOM_GET);
                        marker.setMaxZoom(ZOOM_MAX);
                        marker.setMap(naverMap);
                    }
                });
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
        if (tag.equals(TAG_SURVEY)) {
            if (index == RESULT_PASS) {
                assert text != null;
                LatLng surveyLatLng = convertTmToLatLng(Double.valueOf(text[0]), Double.valueOf(text[1]));
                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(surveyLatLng, ZOOM_DEFAULT).animate(CameraAnimation.Fly);
                naverMap.moveCamera(cameraUpdate);
            } else {
                isSelected = false;
                onBackPressed();
            }
        }
    }

    /**
     * Tm 좌표계를 LatLng 경위도 좌표계로 변환한다. 반드시 변환 과정에서 X, Y 좌표를 반전시켜 리턴한다.
     */
    @NonNull
    private LatLng convertTmToLatLng(double Tm1, double Tm2) {
        GeoPoint surveyPoint = GeoTrans.convert(originPoint, GEO, new GeoPoint(Tm2, Tm1));
        return new LatLng(surveyPoint.getY(), surveyPoint.getX());
    }

    @Override
    public void onBackPressed() {
        if (surveyDialog.isAdded() && isSelected) return;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        originPoint = null;
        surveyDialog = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(null);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events
        }
    }
}
