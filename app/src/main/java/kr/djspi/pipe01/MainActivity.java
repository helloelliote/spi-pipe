package kr.djspi.pipe01;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import kr.djspi.pipe01.nfc.NfcUtil;

import static kr.djspi.pipe01.nfc.NfcUtil.createTag;
import static kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled;

public class MainActivity extends LocationUpdate {

    // TODO: 마무리 이후에 독립 API 만들기
    private static final String TAG = MainActivity.class.getSimpleName();
    public static NfcAdapter nfcAdapter;
    static NfcUtil nfcUtil;

    private Tag tag;

    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = MainActivity.getNfcAdapter(this);
        nfcUtil = NfcUtil.getInstance(nfcAdapter);
        NfcUtil.setDispatch(this, getClass());
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

    private static NfcAdapter getNfcAdapter(Context context) {
        if (nfcAdapter == null) nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter;
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        tag = createTag(intent);
        checkSerialNum();
    }

    private void checkSerialNum() {
//        try {
//            String serialNum = Const.ByteArrayToHexString(mNfcTag.getId());
//            mNfcTag = null;
//
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put(API_KEY_REQUEST, API_REQUEST_SPI_GET);
//
//            JSONObject spiDataList = new JSONObject();
//            spiDataList.put(KEY_SERIAL.getKey(), serialNum);
//            jsonObject.put(API_KEY_DATA, spiDataList);
//
//            RetrofitCore.getInstance()
//                    .setService(new SpiGetService())
//                    .setQuery(jsonObject.toString())
//                    .build(new OnRetrofitListen(callback));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
