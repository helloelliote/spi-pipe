package kr.djspi.pipe01;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.helloelliote.retrofit.Retrofit2x;
import com.helloelliote.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.retrofit.SpiPost;

import java.io.Serializable;
import java.util.ArrayList;

import kr.djspi.pipe01.dto.Entry;
import kr.djspi.pipe01.fragment.MessageDialog;
import kr.djspi.pipe01.nfc.NfcUtil;

import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.nfc.StringParser.parseToStringArray;

public class SpiPostActivity extends BaseActivity implements Serializable {

    private static final String TAG = SpiPostActivity.class.getSimpleName();
    private static ArrayList<Entry> entries;
    private Uri imageFileUri;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra("entry");
        if (serializable instanceof ArrayList<?>) {
            entries = (ArrayList<Entry>) serializable;
        }
        imageFileUri = getIntent().getParcelableExtra("photoUri");
        if (imageFileUri != null) {

        }
        setContentView(R.layout.activity_spi_post);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        TextView textView = findViewById(R.id.txt_write);
        textView.setText(Html.fromHtml(getString(R.string.write_instruction)));

        runOnUiThread(() -> {
            MessageDialog dialog = new MessageDialog();
            dialog.setCancelable(false);
            Bundle bundle = new Bundle(1);
            bundle.putInt("issueType", 5);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), getString(R.string.popup_read_only));
        });
    }

    @Override
    protected boolean useToolbar() {
        return true;
    }

    @Override
    protected void setToolbarTitle(String string) {
        super.setToolbarTitle(string);
    }

    /**
     * 쓰기 대상 태그를 인식시키면 최초로 실행
     *
     * @param intent 전달된 태그 인텐트
     * @see OnRetrofitListener#onResponse(JsonObject) 인텐트를 넘겨받아 처리
     */
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setSpiAndPipe(intent);
    }

    private void setSpiAndPipe(Intent intent) {
        Retrofit2x.builder()
                .setService(new SpiPost(URL_SPI))
                .setQuery(new Gson().toJson(entries))
                .build()
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        // TODO: 2019-03-25 순차적 데이터 입력에 대한 처리 개발
                        processTag(intent, response, 0);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showMessageDialog(7, throwable.getMessage(), true);
                    }

                    /**
                     * 태그에 쓰기 작업을 지시, 결과를 출력
                     * (성공) MainActivity 로 돌아감 + 성공 메시지 표시
                     * (실패) 재시도 요청 토스트 메시지 표시
                     *
                     * @param intent 전달된 태그 인텐트
                     * @see NfcUtil#writeTag(Intent, String[]) 쓰기 작업을 수행, 성공 여부를 리턴
                     */
                    private void processTag(final Intent intent, JsonObject response, int index) {
                        String[] strings = parseToStringArray(response, index);
                        if (nfcUtil.writeTag(intent, strings)) {
                            nfcUtil.onPause();
                            showMessageDialog(6, getString(R.string.popup_write_success), true);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    protected void onResume() {
        super.onResume();
        nfcUtil.onResume();
    }

    @Override
    @SuppressWarnings("EmptyMethod")
    protected void onPause() {
        super.onPause();
    }
}
