package kr.djspi.pipe01.tab;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.helloelliote.json.Json;

import org.jetbrains.annotations.Contract;

import kr.djspi.pipe01.R;

import static android.text.Html.fromHtml;

public class InfoTab extends Fragment {

    private static final String TAG = InfoTab.class.getSimpleName();
    private static JsonObject jsonObject;

    public InfoTab() {
    }

    private static class LazyHolder {
        static final InfoTab INSTANCE = new InfoTab();
    }

    @Contract(pure = true)
    public static InfoTab getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            OnRecordListener listener = (OnRecordListener) context;
            jsonObject = listener.getJsonObjectRecord();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_info, container, false);

        String hDirection;
        switch (jsonObject.get("position").getAsInt()) {
            case 1:
            case 2:
            case 3:
                hDirection = String.format("차도 방향 %s m", jsonObject.get("vertical").getAsString());
                break;
            case 7:
            case 8:
            case 9:
                hDirection = String.format("보도 방향 %s m", jsonObject.get("vertical").getAsString());
                break;
            default:
                hDirection = "";
                break;
        }

        String vDirection;
        switch (jsonObject.get("position").getAsInt()) {
            case 1:
            case 4:
            case 7:
                vDirection = String.format("좌측 %s m", jsonObject.get("horizontal").getAsString());
                break;
            case 3:
            case 6:
            case 9:
                vDirection = String.format("우측 %s m", jsonObject.get("horizontal").getAsString());
                break;
            default:
                vDirection = "";
                break;
        }

        TextView txtContents = view.findViewById(R.id.txt_contents);

        if (hDirection.equals("") && vDirection.equals("")) {
            txtContents.setText(fromHtml(getString(R.string.nfc_info_read_contents_alt,
                    Json.s(jsonObject, "pipe"),
                    jsonObject.get("shape").getAsString(),
                    jsonObject.get("spec").getAsString(),
                    jsonObject.get("unit").getAsString(),
                    jsonObject.get("material").getAsString(),
                    jsonObject.get("spi_type").getAsString(),
                    jsonObject.get("depth").getAsString()
            )));
        } else {
            txtContents.setText(fromHtml(getString(R.string.nfc_info_read_contents,
                    jsonObject.get("pipe").getAsString(),
                    jsonObject.get("shape").getAsString(),
                    jsonObject.get("spec").getAsString(),
                    jsonObject.get("unit").getAsString(),
                    jsonObject.get("material").getAsString(),
                    hDirection,
                    vDirection,
                    jsonObject.get("depth").getAsString()
            )));
        }

        if (jsonObject.get("spi_memo").getAsString() != null) {
            TextView txtMemo = view.findViewById(R.id.txt_memo);
            txtMemo.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            txtMemo.setText(jsonObject.get("spi_memo").getAsString());
        }

        return view;
    }
}
