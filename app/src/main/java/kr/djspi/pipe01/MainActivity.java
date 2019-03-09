package kr.djspi.pipe01;

import android.content.Intent;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;

public class MainActivity extends LocationUpdate implements Serializable {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static NfcUtil nfcUtil;
    private static Tag tag;

    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcUtil = NfcUtil.get(this, getClass()).setNxpLibrary(this);
    }

    /**
     * 메뉴 레이아웃 구성 및 터치를 통한 화면전환 기능 설정
     * 실제 사용환경에서는 NFC 태그의 태깅을 통해서만 화면이 전환되면 되므로 사용하지 않음
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        LinearLayout mainLayout1 = findViewById(R.id.lay_main1);

        LinearLayout mainLayout2 = findViewById(R.id.lay_main2);
        mainLayout2.setOnClickListener(view -> {
            if (currentLocation != null) {
                startActivity(new Intent(context, NaverMapActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } else {
                Toast.makeText(this, "위치 정보를 불러오지 못했습니다.\n" +
                        "잠시후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout mainLayout3 = findViewById(R.id.lay_main3);
//        mainLayout3.setOnClickListener(view ->
//                startActivity(new Intent(context, PipeRecordActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        tag = NfcUtil.onNewTagIntent(intent);
        onNewTag(tag);
    }

    private void onNewTag(@NotNull Tag tag) {
        final String serial = NfcUtil.bytesToHex(tag.getId());
        JsonObject jsonQuery = new JsonObject();
        jsonQuery.addProperty("sp_serial", serial);

        Retrofit2x.builder()
                .setService(new SpiGet(URL_SPI))
                .setQuery(jsonQuery)
                .build()
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        int statusCode = response.get("response").getAsInt();
                        if (statusCode == 400) {
                            // TODO: 2019-03-06 사용할 수 없는 태그
                        }
                        if (statusCode == 200) {
//                            JsonArray jsonArray = response.get("data").getAsJsonArray();
//                            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
//                            String id = jsonObject.get("id").getAsString(); // 304
//                            int type_id = jsonObject.get("type_id").getAsInt(); // 1
//                            여기까지 해서 id, type 를 넣어줄 것으로 가정
                            Spi spi = new Spi(304, serial, 1);
//                            String type = jsonObject.get("type").getAsString(); // "표지판"
                            SpiType spiType = new SpiType(1, "표지판");

                            HashMap<String, DataItem> hashMap = new HashMap<>();
                            hashMap.put("spi", spi);
                            hashMap.put("spi_type", spiType);
                            startActivity(new Intent(context, RecordInputActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    .putExtra("PipeRecordActivity", hashMap));
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showMessagePopup(6, throwable.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        if (!isNfcEnabled()) showMessagePopup(2, getString(R.string.popup_nfc_on));
        nfcUtil.onResume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        nfcUtil.onPause(this);
        super.onPause();
    }

    @Override
    public void onLocationUpdate(Location location) {
    }
}
