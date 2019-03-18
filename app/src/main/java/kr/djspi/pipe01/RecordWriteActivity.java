package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;

import kr.djspi.pipe01.nfc.NfcUtil;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SpiPost;

import static kr.djspi.pipe01.Const.URL_TEST;
import static kr.djspi.pipe01.MainActivity.nfcUtil;

public class RecordWriteActivity extends BaseActivity implements Serializable {

    // TODO: 2019-03-15 통합형으로 관로 정보 전송시 에러 발생하는 관로에 대해서 롤백 및 롤백 안내
    private static final String TAG = RecordWriteActivity.class.getSimpleName();
    private static boolean isSpiSetValid = false;
    private static ArrayList entries;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra("entry");
        if (serializable instanceof ArrayList) entries = (ArrayList) serializable;
        setContentView(R.layout.activity_record_write);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        TextView textView = findViewById(R.id.txt_write);
        textView.setText(Html.fromHtml(getString(R.string.write_instruction)));

        runOnUiThread(() -> RecordWriteActivity.this.showMessageDialog(4, getString(R.string.popup_read_only)));
    }

    @Override
    boolean useToolbar() {
        return true;
    }

    @Override
    protected void setToolbarTitle(String string) {
        super.setToolbarTitle(string);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && setSpiData()) processTag(intent);
        else isSpiSetValid = false;
    }

    private boolean setSpiData() {
        Retrofit2x.builder()
                .setService(new SpiPost(URL_TEST))
                .setQuery(new Gson().toJson(entries))
                .build()
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        Log.w(TAG, response.toString());
                        isSpiSetValid = true;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showMessageDialog(6, throwable.getMessage());
                        isSpiSetValid = false;
                    }
                });
        return isSpiSetValid;
    }

    /**
     * 쓰기 대상 태그에 쓰기 작업을 지시, 결과를 출력
     * (성공) MainActivity 로 돌아감 + 성공 토스트 메시지 표시
     * (실패) 재시도 요청 토스트 메시지 표시
     *
     * @param intent 전달된 태그 인텐트
     * @see NfcUtil#writeTag(Intent, String[]) 쓰기 작업을 수행, 성공 여부를 리턴
     */
    private void processTag(final Intent intent) {
        if (nfcUtil.writeTag(intent, new String[]{})) {
            nfcUtil.onPause(this);
            showMessageDialog(5, getString(R.string.popup_write_success));
        } else {
            Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isSpiSetValid = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isSpiSetValid = false;
    }
}
