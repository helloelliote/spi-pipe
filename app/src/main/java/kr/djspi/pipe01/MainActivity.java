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
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SpiGet;

import static kr.djspi.pipe01.Const.ERROR_CODE_NONE;
import static kr.djspi.pipe01.Const.URL_TEST;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;
import static kr.djspi.pipe01.retrofit2x.ApiKey.API_SPI_GET;

public class MainActivity extends LocationUpdate implements Serializable {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static Tag tag;

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
        if (intent != null) {
            tag = NfcUtil.onNewTagIntent(intent);
            onNewTag(tag);
        }
    }

    private void onNewTag(@NotNull Tag tag) {
        final String serial = NfcUtil.bytesToHex(tag.getId());
        JsonObject jsonQuery = new JsonObject();
        jsonQuery.addProperty("spi_serial", serial);
        try {
            Retrofit2x.builder()
                    .setService(new SpiGet(URL_TEST, API_SPI_GET))
                    .setQuery(jsonQuery)
                    .build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            if (response.get("error_code").getAsInt() == ERROR_CODE_NONE &&
                                    response.get("total_count").getAsInt() == 1) {
                                JsonArray dataArray = response.get("data").getAsJsonArray();
                                JsonObject dataObject = dataArray.get(0).getAsJsonObject();
                                int id = dataObject.get("spi_id").getAsInt();
                                int type_id = dataObject.get("spi_type_id").getAsInt();
                                String spi_type = dataObject.get("spi_type").getAsString();

                                // TODO: 2019-03-20 SpiLocation 고유 id 처리
                                Spi spi = new Spi(id, serial, type_id);
                                SpiType spiType = new SpiType(type_id, spi_type);

                                HashMap<String, DataItem> hashMap = new HashMap<>();
                                hashMap.put("spi", spi);
                                hashMap.put("spiType", spiType);
                                startActivity(new Intent(context, RecordInputActivity2.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        .putExtra("PipeRecordActivity", hashMap));
                            } else showMessageDialog(3, getString(R.string.popup_error_not_spi));
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            showMessageDialog(7, throwable.getMessage());
                        }
                    });
        } catch (IllegalStateException | NullPointerException e) {
            showMessageDialog(7, e.getMessage());
        }
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
}
