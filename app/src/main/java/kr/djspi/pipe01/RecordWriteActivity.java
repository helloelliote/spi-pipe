package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;

import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SpiPost;

import static kr.djspi.pipe01.Const.URL_TEST;

public class RecordWriteActivity extends BaseActivity implements Serializable, OnClickListener {

    private static final String TAG = RecordWriteActivity.class.getSimpleName();
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
        findViewById(R.id.btn_write).setOnClickListener(this);

        runOnUiThread(() -> RecordWriteActivity.this.showMessagePopup(4, getString(R.string.popup_read_only)));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_write:
                Retrofit2x.builder()
                        .setService(new SpiPost(URL_TEST))
                        .setQuery(new Gson().toJson(entries))
                        .build()
                        .run(new OnRetrofitListener() {
                            @Override
                            public void onResponse(JsonObject response) {
                                showMessagePopup(0, response.toString());
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                showMessagePopup(0, throwable.getMessage());
                            }
                        });
                break;
            default:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
