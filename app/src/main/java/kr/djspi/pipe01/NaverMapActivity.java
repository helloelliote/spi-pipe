package kr.djspi.pipe01;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SearchPlacesService;
import com.helloelliote.util.retrofit.SpiGet;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Pickable;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.InfoWindow.DefaultTextAdapter;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.transitionseverywhere.ChangeText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.helloelliote.util.retrofit.ApiKey.API_PIPE_GET;
import static com.naver.maps.map.CameraAnimation.Fly;
import static com.naver.maps.map.LocationTrackingMode.Face;
import static com.naver.maps.map.LocationTrackingMode.Follow;
import static com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING;
import static com.naver.maps.map.NaverMap.MapType;
import static com.naver.maps.map.overlay.OverlayImage.fromResource;
import static com.naver.maps.map.util.MapConstants.EXTENT_KOREA;
import static com.transitionseverywhere.ChangeText.CHANGE_BEHAVIOR_OUT_IN;
import static java.lang.Double.parseDouble;
import static java.util.Objects.requireNonNull;
import static kr.djspi.pipe01.BuildConfig.CLIENT_ID;
import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.dto.PipeType.parsePipeType;

public class NaverMapActivity extends LocationUpdate implements OnMapReadyCallback, Serializable {

    private static final String TAG = NaverMapActivity.class.getSimpleName();
    private static final double ZOOM_DEFAULT = 18.0; // 기본 줌레벨
    private static final double ZOOM_MIN = 6.0; // 최소 줌레벨
    private static final double ZOOM_GET = 12.0;
    private static final double ZOOM_MAX = NaverMap.MAXIMUM_ZOOM; // 최대 줌레벨(21)
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static final int PAD_LEFT = 0;
    static final int PAD_TOP = 45;
    static final int PAD_RIGHT = 0;
    static final int PAD_BOT = 45;
    static BottomSheetBehavior behavior;
    static SetTopSheet.ListViewAdapter placesListAdapter;
    static Overlay.OnClickListener listener;
    final ArrayList<HashMap<String, String>> placesArrayList = new ArrayList<>(5);
    SearchView searchView;

    /**
     * @see NaverMapActivity#setNaverMap() 네이버 지도 구현
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // https://console.ncloud.com/mc/solution/naverService/application 에서 클라이언트 ID 발급
        NaverMapSdk.getInstance(this)
                .setClient(new NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID));
        setContentView(R.layout.activity_navermap);
        setNaverMap();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbar(null);
        useSettingsMenu();
    }

    @Override
    void setToolbar(String title) {
        toolbar.findViewById(R.id.nmap_find).setVisibility(GONE); // '측량점 찾기' 버튼 없앰
    }

    @Override
    boolean useSettingsMenu() {
        return true;
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
                    .locale(Locale.KOREA)
                    .enabledLayerGroups(LAYER_GROUP_BUILDING)
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
     * 네이버 지도의 기초 동작 설정. NaverMapActivity 객체가 준비되면 onMapReady() 콜백 메서드가 호출
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
     *                 getMapAsync() 메서드로 OnMapReadyCallback 을 등록하면 NaverMapActivity 객체를 얻는다.
     * @see NaverMapActivity#setMapModeSwitch(NaverMap)
     */
    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        naverMap.setLocationSource(locationSource);
        // UI 요소에 가려진 영역을 패딩으로 지정하면 카메라는 콘텐츠 패딩을 제외한 영역의 중심에 위치한다.
        naverMap.setLocationTrackingMode(Follow);
        naverMap.addOnOptionChangeListener(() -> {
            LocationTrackingMode mode = naverMap.getLocationTrackingMode();
            locationSource.setCompassEnabled(mode == Follow || mode == Face);
        });
        setMapModeSwitch(naverMap);
        setOverlayListener();
        new SetTopSheet(naverMap);
        new SetBottomSheet(naverMap);
        onRequestPipe(naverMap);
    }

    /**
     * Toolbar 에서 지도 모드 전환 스위치 구현
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
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

    private void setOverlayListener() {
        InfoWindow infoWindow = new InfoWindow(new DefaultTextAdapter(getApplicationContext()) {

            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                if (infoWindow.getMarker() != null && infoWindow.getMarker().getTag() != null) {
                    JsonObject jsonObject = (JsonObject) infoWindow.getMarker().getTag();
                    String pipe = Json.s(jsonObject, "pipe");
                    String id = Json.s(jsonObject, "id");
                    return String.format("%string (%string)", pipe, id);
                }
                return "ERROR";
            }
        });

        infoWindow.setMaxZoom(ZOOM_MAX);
        infoWindow.setMinZoom(14);
        infoWindow.setAlpha(0.85f);
        try {
            listener = overlay -> {
                if (overlay instanceof Marker) {
                    Marker marker = (Marker) overlay;
                    if (marker.getInfoWindow() == null) {
                        // 현재 마커에 정보 창이 열려있지 않을 경우 연다
                        infoWindow.open(marker);
                    } else {
                        // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                        infoWindow.close();
                    }
                }
                if (overlay instanceof InfoWindow) {
                    InfoWindow window = (InfoWindow) overlay;
                    if (infoWindow.getMarker() != null && infoWindow.getMarker().getTag() != null) {
                        JsonObject jsonObject = (JsonObject) infoWindow.getMarker().getTag();
                        startActivity(new Intent(this, ViewActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .putExtra("PipeView", jsonObject.toString()));
                    }
                    window.close();
                }
                return true;
            };
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            infoWindow.close();
            return;
        }
        infoWindow.setOnClickListener(listener);
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
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        if (Json.i(response, "total_count") == 0) {
                            behavior.setState(STATE_COLLAPSED);
                            showMessageDialog(0, "표시할 SPI 정보가 없습니다", true);
                        } else {
                            JsonArray jsonArray = Json.a(response, "data");
                            for (JsonElement element : jsonArray) {
                                JsonObject jsonObject = element.getAsJsonObject();
                                setMarker(jsonObject);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showMessageDialog(8, throwable.getMessage(), true);
                    }

                    private void setMarker(@NonNull JsonObject jsonObject) {
                        double lat = Json.d(jsonObject, "spi_latitude");
                        double lng = Json.d(jsonObject, "spi_longitude");
                        int resId = parsePipeType(Json.s(jsonObject, "pipe")).getDrawRes();
                        Marker marker = new Marker(new LatLng(lat, lng), fromResource(resId));
                        marker.setTag(jsonObject);
                        marker.setMinZoom(ZOOM_GET);
                        marker.setMaxZoom(ZOOM_MAX);
                        marker.setOnClickListener(listener);
                        marker.setMap(naverMap);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (behavior.getState() == STATE_EXPANDED) {
            behavior.setState(STATE_COLLAPSED);
            return;
        } else if (placesArrayList.size() != 0) {
            placesArrayList.clear();
            placesListAdapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();
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

    private final class SetTopSheet {

        SetTopSheet(NaverMap naverMap) {
            ListView listView = findViewById(R.id.nmap_listView);
            placesListAdapter = new ListViewAdapter(getApplicationContext(), naverMap, placesArrayList);
            listView.setAdapter(placesListAdapter);

            setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
            searchView = findViewById(R.id.nmap_searchView);
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(new OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    setSearchPlaces(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }

        private void setSearchPlaces(String query) {
            final LatLng latLng = new LatLng(currentLocation);
            final String coordinate = String.format(
                    "%string,%string",
                    Double.toString(latLng.longitude),
                    Double.toString(latLng.latitude));
            JsonObject jsonQuery = new JsonObject();
            jsonQuery.addProperty("place", query);
            jsonQuery.addProperty("coordinate", coordinate);

            Retrofit2x.builder()
                    .setService(new SearchPlacesService())
                    .setQuery(jsonQuery)
                    .build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            placesArrayList.clear();
                            behavior.setState(STATE_COLLAPSED);
                            JsonArray places = Json.a(response, "places");
                            if (places == null || places.size() == 0) {
                                showMessageDialog(0, getString(R.string.popup_error_noplace), true);
                                return;
                            }
                            for (JsonElement place : places) {
                                JsonObject object = place.getAsJsonObject();
                                HashMap<String, String> hashMap = new HashMap<>(3);
                                hashMap.put("name", Json.s(object, "name"));
                                hashMap.put("x", Json.s(object, "x"));
                                hashMap.put("y", Json.s(object, "y"));
                                placesArrayList.add(hashMap);
                            }
                            placesListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            showMessageDialog(8, throwable.getMessage(), true);
                        }
                    });
        }

        private final class ListViewAdapter extends BaseAdapter {

            private final List<HashMap<String, String>> placesList;
            private final LayoutInflater inflater;
            private final NaverMap naverMap;

            ListViewAdapter(Context context, NaverMap naverMap, ArrayList<HashMap<String, String>> placesArrayList) {
                this.placesList = placesArrayList;
                this.naverMap = naverMap;
                this.inflater = LayoutInflater.from(context);
            }

            class ItemHolder {
                TextView name;
            }

            @Override
            public int getCount() {
                return placesList.size();
            }

            @Override
            public Object getItem(int position) {
                return placesList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            @SuppressLint("InflateParams")
            public View getView(final int position, View convertView, ViewGroup parent) {
                final ItemHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.list_searchplaces, null);
                    holder = new ItemHolder();
                    holder.name = convertView.findViewById(R.id.name);
                    convertView.setTag(holder);
                } else {
                    holder = (ItemHolder) convertView.getTag();
                }
                holder.name.setText(placesList.get(position).get("name"));
                convertView.setOnClickListener((View v) -> {
                    // searchView 내용 변경과 동시에 Query 를 다시 시작하려면 true
                    searchView.setQuery(placesList.get(position).get("name"), false);
                    final double coordinate_x = parseDouble(requireNonNull(placesList.get(position).get("x")));
                    final double coordinate_y = parseDouble(requireNonNull(placesList.get(position).get("y")));
                    double zoom = naverMap.getCameraPosition().zoom;
                    zoom = zoom < ZOOM_GET ? ZOOM_GET + 1.0 : zoom;
                    naverMap.moveCamera(CameraUpdate
                            .scrollAndZoomTo(new LatLng(coordinate_y, coordinate_x), zoom)
                            .animate(Fly)
                            .finishCallback(() -> {
                                behavior.setState(STATE_EXPANDED);
                                searchView.clearFocus();
                            }));
                    placesList.clear();
                    placesListAdapter.notifyDataSetChanged();
                });
                return convertView;
            }
        }
    }

    private final class SetBottomSheet {

        private final PointF POINT_F = new PointF(0.5f, 0.5f);
        private final Transition TRANSITION_TEXT = new ChangeText().setChangeBehavior(CHANGE_BEHAVIOR_OUT_IN);

        SetBottomSheet(NaverMap naverMap) {
            LinearLayout bottomSheetView = findViewById(R.id.nmap_bottom_sheet);
            TextView bottomSheetText = findViewById(R.id.nmap_bottom_sheet_text);
            bottomSheetText.setOnClickListener((View view) -> {
                switch (behavior.getState()) {
                    case STATE_COLLAPSED:
                        if (naverMap.getCameraPosition().zoom < ZOOM_GET) {
                            showMessageDialog(0, getString(R.string.popup_error_zoom), true);
                            return;
                        }
                        behavior.setState(STATE_EXPANDED);
                        break;
                    case STATE_EXPANDED:
                        behavior.setState(STATE_COLLAPSED);
                        break;
                    default:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            });
            String textExpanded = getString(R.string.map_search_input);
            String textCollapsed = getString(R.string.map_search_point);
            final int bottomSheetHeight = bottomSheetView.getHeight();
            behavior = BottomSheetBehavior.from(bottomSheetView);
            behavior.setBottomSheetCallback(new BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View view, int newState) {
                    TransitionManager.beginDelayedTransition(bottomSheetView, TRANSITION_TEXT);
                    switch (newState) {
                        case STATE_EXPANDED:
                            bottomSheetText.setText(textExpanded);
                            naverMap.setContentPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, bottomSheetHeight);
                            onRequestPipe(naverMap);
                            break;
                        case STATE_COLLAPSED:
                            bottomSheetText.setText(textCollapsed);
                            naverMap.setContentPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOT);
                            clearMarker();
                            break;
                        default:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_HALF_EXPANDED:
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                }

                private void clearMarker() {
                    List<Pickable> overlayList = naverMap.pickAll(POINT_F, getScreen());
                    for (Pickable overlay : overlayList) {
                        if (overlay instanceof Marker) ((Marker) overlay).setMap(null);
                    }
                }

                private int getScreen() {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    final int screenY = displayMetrics.heightPixels;
                    final int screenX = displayMetrics.widthPixels;
                    return screenY > screenX ? screenY : screenX;
                }
            });
        }
    }
}
