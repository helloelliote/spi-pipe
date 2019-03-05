package kr.djspi.pipe01;

import android.content.Intent;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore;
import kr.djspi.pipe01.retrofit2x.SpiGetService;

import static kr.djspi.pipe01.NaverMapActivity.URL_SPI;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;

public class MainActivity extends LocationUpdate {

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
        nfcUtil = NfcUtil.getInstance(this, getClass()).initializeLibrary(this);
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
        mainLayout3.setOnClickListener(view ->
                startActivity(new Intent(context, PipeRecordActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        tag = NfcUtil.intentToTag(intent);
        if (isValidSpi()) processSpi(tag);
    }

    @Contract(pure = true)
    private boolean isValidSpi() {
//        try {
//            String serialNum = Const.ByteArrayToHexString(mNfcTag.getId());
////            mNfcTag = null;
////
////            JSONObject jsonObject = new JSONObject();
////            jsonObject.put(API_KEY_REQUEST, API_REQUEST_SPI_GET);
////
////            JSONObject spiDataList = new JSONObject();
////            spiDataList.put(KEY_SERIAL.getKey(), serialNum);
////            jsonObject.put(API_KEY_DATA, spiDataList);
////
////            RetrofitCore.getInstance()
////                    .setService(new SpiGetService())
////                    .setQuery(jsonObject.toString())
////                    .build(new OnRetrofitListen(callback));
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
        return true;
    }

    private void processSpi(@NotNull Tag tag) {
        final String serial = NfcUtil.bytesToHex(tag.getId());
        JsonObject jsonQuery = new JsonObject();
        // TODO: 2019-03-05 sp_ 등의 헤더 없애기
        jsonQuery.addProperty("sp_serial", serial);

        Retrofit2x.newBuilder()
                .setService(new SpiGetService(URL_SPI))
                .setQuery(jsonQuery)
                .build()
                .run(new RetrofitCore.OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        Log.w(TAG, response.toString());
                        int statusCode = response.get("response").getAsInt();
                        if (statusCode == 400) {
                            startActivity(new Intent(context, PipeRecordActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    .putExtra("PipeRecordActivity", ""));
                        }
                        // TODO: 2019-03-05 시리얼 번호로 조회하여 없으면 신규, 있으면 정보 읽기로?
                        if (statusCode == 200) {

                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showMessagePopup(0, throwable.getMessage());
                    }
                });
    }

    /**
     * (필수) NFC 기능을 사용할 Activity 의 onResume 에서 호출 또는 사용시
     */
    @Override
    @SuppressWarnings("EmptyMethod")
    public void onResume() {
        if (!isNfcEnabled()) showMessagePopup(2, getString(R.string.popup_nfc_on));
        nfcUtil.onResume(this);
        super.onResume();
    }

    /**
     * (필수) NFC 기능을 사용할 Activity 의 onPause 에서 호출 또는 사용 완료시
     */
    @Override
    @SuppressWarnings("EmptyMethod")
    public void onPause() {
        nfcUtil.onPause(this);
        super.onPause();
    }

    @Override
    void onLocationUpdate(Location location) {
    }

//    private final class OnRetrofitListen implements OnRetrofitListener {
//
//        private Handler callback;
//
//        private OnRetrofitListen(Handler callback) {
//            this.callback = callback;
//        }
//
//        @Override
//        public void onResponse(JsonObject response) {
//            SPIDataSet spiDataSet = new SPIDataSet(context, response.toString());
//            if (spiDataSet.getCount() != HAS_SPI) {
//                showMessagePopup(0, spiDataSet.getError());
//            } else {
//                if (callback == null) {
//                    SPIData spiData = spiDataSet.getArrayList().getInstance(0);
//                    startActivity(new Intent(context, NfcRecordInput.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                            .putExtra("NfcRecordInput", spiData));
//                } else {
//                    callback.obtainMessage(0, spiDataSet.getArrayList().getInstance(0)).sendToTarget();
//                }
//            }
//        }
//
//        @Override
//        public void onFailure(Throwable throwable) {
//            showMessagePopup(0, getString(R.string.popup_api_error));
//            throwable.printStackTrace();
//        }
//    }

    private final class setupNfc {

    }
}
