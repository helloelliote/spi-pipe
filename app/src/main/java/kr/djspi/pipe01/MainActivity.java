package kr.djspi.pipe01;

import android.content.Intent;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;

import kr.djspi.pipe01.dto.DataItem;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMemo;
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SpiGet;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.ERROR_CODE_NONE;
import static kr.djspi.pipe01.Const.URL_TEST;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;
import static kr.djspi.pipe01.retrofit2x.ApiKey.API_PIPE_GET;
import static kr.djspi.pipe01.retrofit2x.ApiKey.API_SPI_GET;

public class MainActivity extends LocationUpdate implements Serializable {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 메뉴 레이아웃 구성 및 터치를 통한 화면전환 기능 설정
     * 실제 사용환경에서는 NFC 태그의 태깅을 통해서만 화면이 전환되면 되므로 사용하지 않음
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

//        LinearLayout mainLayout1 = findViewById(R.id.lay_main1);

        LinearLayout mainLayout2 = findViewById(R.id.lay_main2);
        mainLayout2.setOnClickListener(view -> {
            if (currentLocation == null) {
                Toast.makeText(this, getString(R.string.toast_error_location), Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(this, NaverMapActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });

//        LinearLayout mainLayout3 = findViewById(R.id.lay_main3);
//        mainLayout3.setOnClickListener(view ->
//                startActivity(new Intent(context, RecordInputActivity2.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));

    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) new ProcessTag(intent);
    }

    @Override
    public void onResume() {
        if (!isNfcEnabled()) showMessageDialog(2, getString(R.string.popup_nfc_on));
        super.onResume();
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLocationUpdate(Location location) {
    }

    private final class ProcessTag {

        private final String serial;
        private final JsonObject jsonQuery = new JsonObject();

        ProcessTag(Intent intent) {
            progressBar.setVisibility(VISIBLE);
            Tag tag = NfcUtil.onNewTagIntent(intent);
            this.serial = NfcUtil.bytesToHex(tag.getId());
            this.jsonQuery.addProperty("spi_serial", serial);
            getServerData();
        }

        private void getServerData() {
            Retrofit2x.builder()
                    .setService(new SpiGet(URL_TEST, API_SPI_GET))
                    .setQuery(jsonQuery).build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            if (response.get("error_code").getAsInt() == ERROR_CODE_NONE
                                    && response.get("total_count").getAsInt() >= 1) {
                                processServerData(response);
                            } else {
                                showMessageDialog(3, getString(R.string.popup_error_not_spi));
                                progressBar.setVisibility(GONE);
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            showMessageDialog(7, throwable.getMessage());
                        }
                    });
        }

        private void processServerData(@NotNull JsonObject response) {
            JsonObject jsonObject = response.get("data").getAsJsonArray().get(0).getAsJsonObject();
            if (jsonObject.get("pipe_count").getAsInt() == 0) {
                startActivity(new Intent(context, RecordInputActivity2.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("PipeRecordActivity2", parseServerData(response)));
                progressBar.setVisibility(GONE);
            } else {
                Retrofit2x.builder()
                        .setService(new SpiGet(URL_TEST, API_PIPE_GET))
                        .setQuery(jsonQuery).build()
                        .run(new OnRetrofitListener() {
                            @Override
                            public void onResponse(JsonObject response) {
                                JsonArray elements = response.get("data").getAsJsonArray();
                                startActivity(new Intent(context, RecordViewActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        .putExtra("RecordViewActivity", elements.get(0).toString()));
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                showMessageDialog(7, throwable.getMessage());
                            }
                        });
                progressBar.setVisibility(GONE);
            }
        }

        private HashMap<String, DataItem> parseServerData(@NotNull JsonObject response) {
            HashMap<String, DataItem> hashMap = new HashMap<>(4);
            JsonArray dataArray = response.get("data").getAsJsonArray();
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            // Spi.class DTO
            int spi_id = dataObject.get("spi_id").getAsInt();
            int type_id = dataObject.get("spi_type_id").getAsInt();
            Spi spi = new Spi(spi_id, serial, type_id);
            // SpiType.class DTO
            String spi_type = dataObject.get("spi_type").getAsString();
            SpiType spiType = new SpiType(type_id, spi_type);
            // SpiLocation.class DTO
            SpiLocation spiLocation = new SpiLocation();
            if (dataObject.get("spi_location_id").isJsonNull()) spiLocation = null;
            else {
                int location_id = dataObject.get("spi_location_id").getAsInt();
                double latitude = dataObject.get("spi_latitude").getAsDouble();
                double longitude = dataObject.get("spi_longitude").getAsDouble();
                int count = dataObject.get("spi_count").getAsInt();
                spiLocation.setId(location_id);
                spiLocation.setSpi_id(spi_id);
                spiLocation.setLatitude(latitude);
                spiLocation.setLongitude(longitude);
                spiLocation.setCount(count);
            }
            // SpiMemo.class DTO
            SpiMemo spiMemo = new SpiMemo();
            if (dataObject.get("spi_memo_id").isJsonNull()) spiMemo = null;
            else {
                int memo_id = dataObject.get("spi_memo_id").getAsInt();
                String memo = dataObject.get("spi_memo").getAsString();
                spiMemo.setId(memo_id);
                spiMemo.setSpi_id(spi_id);
                spiMemo.setMemo(memo);
            }
            hashMap.put("Spi", spi);
            hashMap.put("SpiType", spiType);
            hashMap.put("SpiLocation", spiLocation);
            hashMap.put("SpiMemo", spiMemo);
            return hashMap;
        }
    }
}
