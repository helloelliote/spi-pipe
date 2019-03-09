package kr.djspi.pipe01;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Toast;

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMap.MapType;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.MarkerIcons;

import java.io.Serializable;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.naver.maps.map.LocationTrackingMode.Follow;
import static com.naver.maps.map.LocationTrackingMode.NoFollow;
import static com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING;
import static com.naver.maps.map.util.MapConstants.EXTENT_KOREA;
import static kr.djspi.pipe01.BuildConfig.NAVER_CLIENT_ID;

public class SpiLocationActivity extends LocationUpdate implements OnMapReadyCallback, Serializable {

    private static final String TAG = SpiLocationActivity.class.getSimpleName();
    private static final double ZOOM_DEFAULT = 19.0; // 기본 줌레벨
    private static final double ZOOM_MIN = 15.0; // 최소 줌레벨
    private static final double ZOOM_MAX = NaverMap.MAXIMUM_ZOOM; // 최대 줌레벨(21)
    private static Marker currentMarker;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static final int PAD_LEFT = 0;
    static final int PAD_TOP = 0;
    static final int PAD_RIGHT = 0;
    static final int PAD_BOT = 0;
    static Overlay.OnClickListener listener;
    static LatLng currentLatLng;
    static double value_x, value_y;

    /**
     * @see SpiLocationActivity#setNaverMap() 네이버 지도 구현
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // https://console.ncloud.com/mc/solution/naverService/application 에서 클라이언트 ID 발급
        NaverMapSdk.getInstance(this)
                .setClient(new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));
        setContentView(R.layout.activity_spilocation);
        setNaverMap();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findViewById(R.id.nmap_find).setVisibility(GONE); // '측량점 찾기' 버튼 없앰
        setToolbarTitle("SPI 위치설정");
    }

    @Override
    boolean useNavigationView() {
        return false;
    }

    @Override
    protected void setToolbarTitle(String string) {
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
                    .contentPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOT)
                    .camera(new CameraPosition(new LatLng(currentLocation), ZOOM_DEFAULT, 0, 0)) // 현재 내 위치 센터
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
        naverMap.setLocationSource(locationSource);
        // UI 요소에 가려진 영역을 패딩으로 지정하면 카메라는 콘텐츠 패딩을 제외한 영역의 중심에 위치한다.
        naverMap.setLocationTrackingMode(Follow);
        naverMap.addOnOptionChangeListener(() -> {
            LocationTrackingMode mode = naverMap.getLocationTrackingMode();
            locationSource.setCompassEnabled(mode == NoFollow);
        });
        currentLatLng = new LatLng(currentLocation);
        setMapModeSwitch(naverMap);
        new SetBottomSheet(naverMap);
        setMarker(naverMap);
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

    private void setMarker(NaverMap naverMap) {
        // TODO: 2019-03-10 마커 한 가운데에 놓기
        if (currentLatLng == null) return;
        if (currentMarker == null) {
            currentMarker = new Marker(currentLatLng, MarkerIcons.RED);
        } else currentMarker.setPosition(currentLatLng);
        currentMarker.setMap(naverMap);
    }

    @Override
    public void onLocationUpdate(Location location) {
        currentLatLng = new LatLng(location);
    }

    private final class SetBottomSheet {

        SetBottomSheet(NaverMap naverMap) {
            findViewById(R.id.spi_bottom_sheet).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "위치 지정", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
