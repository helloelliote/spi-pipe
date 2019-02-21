package kr.djspi.pipe01;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.Toast;

import kr.djspi.pipe01.util.NfcUtil;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;

public class MainActivity extends LocationUpdateActivity {

    // TODO: 마무리 이후에 독립 API 만들기
    private static final String TAG = MainActivity.class.getSimpleName();
    private Tag mNfcTag;

    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateMenuLayout();
    }

    /**
     * 메뉴 레이아웃 구성 및 터치를 통한 화면전환 기능 설정
     * 실제 사용환경에서는 NFC 태그의 태깅을 통해서만 화면이 전환되면 되므로 사용하지 않음
     */
    private void onCreateMenuLayout() {
        LinearLayout mainLayout1 = findViewById(R.id.lay_main1);
        mainLayout1.setOnClickListener(view -> {
//                startActivity(new Intent(context, NfcRecordRead.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });

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
                startActivity(new Intent(context, SpiInputActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
    }

    public NfcUtil getNFCUtil() {
        return nfcUtil;
    }

    // TODO: 2019-01-29 네트워크 검사는 좀 더 앞 단계에서 수행해야 한다.
    private static boolean checkNetworkState(@NonNull Context context) throws RuntimeException {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(TYPE_WIFI);
        NetworkInfo lte = manager.getNetworkInfo(TYPE_WIMAX);

        return mobile != null && mobile.isConnected() || wifi != null && wifi.isConnected() || lte != null && lte.isConnected();
        // FIXME: 2018-12-22 최소 SDK 23 으로 넘어가면 교체
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        Network network = connectivityManager.getActiveNetwork();
//        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
//        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        mNfcTag = nfcUtil.intentToTag(intent);
        checkSerialNum(null);
    }

    private void checkSerialNum(final Handler callback) {
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
//            RetrofitCore.get()
//                    .setService(new SpiGetService())
//                    .setQuery(jsonObject.toString())
//                    .build(new OnRetrofitListen(callback));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    public void onResume() {
//        if (!isNfcEnabled()) showMessagePopup(2, getString(R.string.popup_nfc_on));
        super.onResume();
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    public void onPause() {
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
//                    SPIData spiData = spiDataSet.getArrayList().get(0);
//                    startActivity(new Intent(context, NfcRecordInput.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                            .putExtra("NfcRecordInput", spiData));
//                } else {
//                    callback.obtainMessage(0, spiDataSet.getArrayList().get(0)).sendToTarget();
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
}
