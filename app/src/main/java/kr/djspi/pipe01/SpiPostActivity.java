package kr.djspi.pipe01;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.helloelliote.util.retrofit.ProgressBody;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SpiDel;
import com.helloelliote.util.retrofit.SpiPost;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import kr.djspi.pipe01.dto.Entry;
import kr.djspi.pipe01.dto.SpiPhotoObject;
import kr.djspi.pipe01.fragment.MessageDialog;
import kr.djspi.pipe01.nfc.NfcUtil;
import okhttp3.MultipartBody;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.R.color.colorAccent;
import static kr.djspi.pipe01.R.color.green;
import static kr.djspi.pipe01.R.color.yellow;
import static kr.djspi.pipe01.dto.Entry.parseEntry;
import static kr.djspi.pipe01.nfc.StringParser.parseToStringArray;

public class SpiPostActivity extends BaseActivity implements Serializable, ProgressBody.UploadCallback {

    private static final String TAG = SpiPostActivity.class.getSimpleName();
    private static final int RED = resources.getColor(colorAccent);
    private static final int YELLOW = resources.getColor(yellow);
    private static final int GREEN = resources.getColor(green);
    private static ArrayList<Entry> entries;
    private JsonObject jsonObject;
    private File file;
    private ProgressBar progressBar;
    private Drawable progressDrawable;
    private TextView textView;
    private TextView progressText;
    private MultipartBody.Part part;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Serializable arraySerializable = getIntent().getSerializableExtra("entry");
        if (arraySerializable instanceof ArrayList<?>) {
            entries = (ArrayList<Entry>) arraySerializable;
            jsonObject = parseEntry(entries, 0, "", "");
        }

        Serializable classSerializable = getIntent().getSerializableExtra("SpiPhotoObject");
        if (classSerializable instanceof SpiPhotoObject) {
            file = ((SpiPhotoObject) classSerializable).getFile();
        }

        part = getMultipart(file, "image");

        setContentView(R.layout.activity_spi_post);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        textView = findViewById(R.id.txt_write);
        textView.setText(Html.fromHtml(getString(R.string.write_instruction)));

        progressBar = findViewById(R.id.progressBar);
        progressDrawable = ((LayerDrawable) progressBar.getProgressDrawable()).getDrawable(1);
        progressDrawable.setTint(YELLOW);
        progressText = findViewById(R.id.progressBar_text);

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
    protected void setToolbar(String string) {
        super.setToolbar(string);
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
        if (intent == null) return;
        if (processTag(intent, jsonObject, 0)) {
            setSpiAndPipe();
        } else showMessageDialog(0, getString(R.string.popup_write_retry), false);
    }

    @SuppressWarnings("SameParameterValue")
    @Contract("null, _ -> null; !null, _ -> !null")
    private MultipartBody.Part getMultipart(File file, String fileType) {
        if (file == null) return null;
        try {
            part = MultipartBody.Part.createFormData(
                    "file", file.getName(), new ProgressBody(file, fileType, this));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return part;
    }

    private void setSpiAndPipe() {
        progressText.setVisibility(VISIBLE);
        onInitiate(0);
        Retrofit2x.builder()
                .setService(new SpiPost(URL_SPI))
                .setQuery(new Gson().toJson(entries), part)
                .build()
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        onFinish(100);
                        progressText.setVisibility(INVISIBLE);
//                        processTag(intent, response, 0);
                        showMessageDialog(6, getString(R.string.popup_write_success), false);
                        if (file != null && file.exists()) file.delete();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        onError();
                        progressText.setVisibility(INVISIBLE);
                        showMessageDialog(7, throwable.getMessage(), true);
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * 태그에 쓰기 작업을 지시, 결과를 출력
     * (성공) MainActivity 로 돌아감 + 성공 메시지 표시
     * (실패) 재시도 요청 토스트 메시지 표시
     *
     * @param intent 전달된 태그 인텐트
     * @see NfcUtil#writeTag(Intent, String[]) 쓰기 작업을 수행, 성공 여부를 리턴
     */
    private boolean processTag(final Intent intent, JsonObject response, int index) {
        boolean isWriteSuccess = false;
        String[] strings = parseToStringArray(response, index);
        if (nfcUtil.writeTag(intent, strings)) {
            isWriteSuccess = true;
//            nfcUtil.onPause();
        }
//            deleteSpi(index);
        return isWriteSuccess;
    }

    @SuppressWarnings("SameParameterValue")
    private void deleteSpi(int pipeIndex) {
        progressText.setVisibility(VISIBLE);
        onInitiate(0);
        Entry currentEntry = entries.get(pipeIndex);
        JsonObject jsonQuery = new JsonObject();
        jsonQuery.addProperty("id", currentEntry.getSpi().getId());
        currentEntry.getSpi().setId(-1);
        currentEntry.getPipe().setSpi_id(-1);
        currentEntry.getSpi_location().setSpi_id(-1);
        currentEntry.getSpi_memo().setSpi_id(-1);
        currentEntry.getSpi_photo().setSpi_id(-1);
        entries.set(pipeIndex, currentEntry);
        Retrofit2x.builder()
                .setService(new SpiDel(URL_SPI))
                .setQuery(jsonQuery).build()
                .run(new OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        textView.setText(Html.fromHtml(getString(R.string.popup_write_retry)));
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onInitiate(0);
    }

    @Override
    public void onInitiate(int percentage) {
        progressBar.setProgress(percentage);
        progressDrawable.setTint(YELLOW);
    }

    @Override
    public void onProgress(int percentage) {
        progressBar.setProgress(percentage);
    }

    @Override
    public void onError() {
        progressDrawable.setTint(RED);
    }

    @Override
    public void onFinish(int percentage) {
        progressBar.setProgress(percentage);
        progressDrawable.setTint(GREEN);
    }
}
