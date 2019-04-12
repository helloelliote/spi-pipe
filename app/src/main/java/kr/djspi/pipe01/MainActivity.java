package kr.djspi.pipe01;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SpiGet;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import kr.djspi.pipe01.dto.DataItem;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMemo;
import kr.djspi.pipe01.dto.SpiPhoto;
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.nfc.NfcUtil;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.helloelliote.util.retrofit.ApiKey.API_PIPE_GET;
import static com.helloelliote.util.retrofit.ApiKey.API_SPI_GET;
import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.nfc.NfcUtil.getRecord;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;
import static kr.djspi.pipe01.nfc.StringParser.parseToJsonObject;

public class MainActivity extends LocationUpdate implements Serializable {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isNetworkConnected;
    private Context context;
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setNetworkCallback();
        setContentView(R.layout.activity_main);
    }

    // TODO: 2019-04-12 서버 상태에 따라서도 나눌 수 있는지 연구
    private void setNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder().build();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                super.onAvailable(network);
                isNetworkConnected = true;
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                isNetworkConnected = false;
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                isNetworkConnected = false;
            }
        };
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    /**
     * 메뉴 레이아웃 구성 및 터치를 통한 화면전환 기능 설정
     * 실제 사용환경에서는 NFC 태그의 태깅을 통해서만 화면이 전환되면 되므로 사용하지 않음
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        LinearLayout mainLayout1 = findViewById(R.id.lay_main1);
        mainLayout1.setOnClickListener(view -> {
            progressBar.setVisibility(VISIBLE);
            if (!isNetworkConnected) {
                showMessageDialog(8, "", true);
                progressBar.setVisibility(INVISIBLE);
            } else if (currentLocation == null) {
                Toast.makeText(this, getString(R.string.toast_error_location), Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(this, NaverMapActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });

        LinearLayout mainLayout2 = findViewById(R.id.lay_main2);
        mainLayout2.setOnClickListener(view -> {
            Toast toast = Toast.makeText(context, getString(R.string.toast_spi_tag), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressBar.getVisibility() == VISIBLE) progressBar.setVisibility(INVISIBLE);
        if (!isNfcEnabled()) showMessageDialog(2, getString(R.string.popup_nfc_on), false);
        if (nfcUtil != null) nfcUtil.onResume();
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressBar.setVisibility(INVISIBLE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public void onLocationUpdate(Location location) {
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        progressBar.setVisibility(VISIBLE);
        if (isNetworkConnected) {
            new ProcessTag(intent);
        } else new ProcessTagOffline(intent, 0);
    }

    private final class ProcessTag {

        private final String serial;
        private final JsonObject jsonQuery = new JsonObject();

        ProcessTag(Intent intent) {
            Tag tag = NfcUtil.onNewTagIntent(intent);
            serial = NfcUtil.bytesToHex(tag.getId());
            jsonQuery.addProperty("spi_serial", serial);
            getServerData();
        }

        private void getServerData() {
            Retrofit2x.builder()
                    .setService(new SpiGet(URL_SPI, API_SPI_GET))
                    .setQuery(jsonQuery).build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            if (Json.i(response, "total_count") >= 1) {
                                processServerData(response);
                            } else {
                                showMessageDialog(3, getString(R.string.popup_error_not_spi), false);
                                progressBar.setVisibility(INVISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Throwable throwable) {
                            showMessageDialog(8, throwable.getMessage(), true);
                            throwable.printStackTrace();
                            progressBar.setVisibility(INVISIBLE);
                        }
                    });
        }

        private void processServerData(@NotNull JsonObject response) {
            JsonObject jsonObject = Json.o(response, "data");
            if (Json.i(jsonObject, "pipe_count") == 0) {
                startActivity(new Intent(context, RegisterActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("RegisterActivity", parseServerData(response)));
                progressBar.setVisibility(INVISIBLE);
            } else {
                Retrofit2x.builder()
                        .setService(new SpiGet(URL_SPI, API_PIPE_GET))
                        .setQuery(jsonQuery).build()
                        .run(new OnRetrofitListener() {
                            @Override
                            public void onResponse(JsonObject response) {
                                JsonArray elements = Json.a(response, "data");
                                // TODO: 2019-04-11 Json 데이터 필드명 순서?
                                startActivity(new Intent(context, ViewActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        .putExtra("PipeView", elements.get(0).toString()));
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                showMessageDialog(8, throwable.getMessage(), true);
                            }
                        });
                progressBar.setVisibility(INVISIBLE);
            }
        }

        private HashMap<String, DataItem> parseServerData(@NotNull JsonObject response) {
            HashMap<String, DataItem> hashMap = new HashMap<>();
            JsonObject data = Json.o(response, "data");
            // Spi.class DTO
            int spi_id = Json.i(data, "spi_id");
            int type_id = Json.i(data, "spi_type_id");
            Spi spi = new Spi(spi_id, serial, type_id);
            // SpiType.class DTO
            String spi_type = Json.s(data, "spi_type");
            SpiType spiType = new SpiType(type_id, spi_type);
            // SpiLocation.class DTO
            SpiLocation spiLocation = new SpiLocation();
            if (!Json.isNull(data, "spi_location_id")) {
                int location_id = Json.i(data, "spi_location_id");
                double latitude = Json.d(data, "spi_latitude");
                double longitude = Json.d(data, "spi_longitude");
                int count = Json.i(data, "spi_count");
                spiLocation.setId(location_id);
                spiLocation.setSpi_id(spi_id);
                spiLocation.setLatitude(latitude);
                spiLocation.setLongitude(longitude);
                spiLocation.setCount(count);
            }
            // SpiMemo.class DTO
            SpiMemo spiMemo = new SpiMemo();
            if (!Json.isNull(data, "spi_memo_id")) {
                int memo_id = Json.i(data, "spi_memo_id");
                String memo = Json.s(data, "spi_memo");
                spiMemo.setId(memo_id);
                spiMemo.setSpi_id(spi_id);
                spiMemo.setMemo(memo);
            }
            // SpiPhoto.class DTO
            SpiPhoto spiPhoto = new SpiPhoto();
            if (!Json.isNull(data, "spi_photo_id")) {
                int photo_id = Json.i(data, "spi_photo_id");
                String url = Json.s(data, "spi_photo_url");
                spiPhoto.setId(photo_id);
                spiPhoto.setSpi_id(spi_id);
                spiPhoto.setUrl(url);
            }
            hashMap.put("Spi", spi);
            hashMap.put("SpiType", spiType);
            hashMap.put("SpiLocation", spiLocation);
            hashMap.put("SpiMemo", spiMemo);
            hashMap.put("SpiPhoto", spiPhoto);
            return hashMap;
        }
    }

    private final class ProcessTagOffline {

        private int index;

        ProcessTagOffline(Intent intent, int index) {
            this.index = index;
            processTagData(intent);
        }

        private void processTagData(Intent intent) {
            try {
                ArrayList<String> stringArrayList = getRecord(intent);
                stringArrayList.remove(0);
                JsonObject data = parseToJsonObject(stringArrayList, index);
                startActivity(new Intent(context, ViewActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("PipeView", data.toString()));
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                showMessageDialog(4, getString(R.string.popup_error_offline_read_error), true);
                Log.w(TAG, e.toString());
            }
        }
    }
}
