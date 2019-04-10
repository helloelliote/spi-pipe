package kr.djspi.pipe01.tab;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;

import kr.djspi.pipe01.R;

import static android.text.Html.fromHtml;

public class InfoTab extends Fragment {

    private static final String TAG = InfoTab.class.getSimpleName();
    private JsonObject jsonObject;
    private Uri imageFileUri;

    public InfoTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            OnRecordListener listener = (OnRecordListener) context;
            jsonObject = listener.getJsonObject();
            imageFileUri = listener.getUri();
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
        switch (Json.i(jsonObject, "position")) {
            case 1:
            case 2:
            case 3:
                hDirection = String.format("차도 방향 %s m", Json.s(jsonObject, "vertical"));
                break;
            case 7:
            case 8:
            case 9:
                hDirection = String.format("보도 방향 %s m", Json.s(jsonObject, "vertical"));
                break;
            default:
                hDirection = "";
                break;
        }

        String vDirection;
        switch (Json.i(jsonObject, "position")) {
            case 1:
            case 4:
            case 7:
                vDirection = String.format("좌측 %s m", Json.s(jsonObject, "horizontal"));
                break;
            case 3:
            case 6:
            case 9:
                vDirection = String.format("우측 %s m", Json.s(jsonObject, "horizontal"));
                break;
            default:
                vDirection = "";
                break;
        }

        TextView txtContents = view.findViewById(R.id.txt_contents);

        if (hDirection.equals("") && vDirection.equals("")) {
            txtContents.setText(fromHtml(getString(R.string.nfc_info_read_contents_alt,
                    Json.s(jsonObject, "pipe"),
                    Json.s(jsonObject, "shape"),
                    Json.s(jsonObject, "spec"),
                    Json.s(jsonObject, "unit"),
                    Json.s(jsonObject, "material"),
                    Json.s(jsonObject, "spi_type"),
                    Json.s(jsonObject, "depth")
            )));
        } else {
            txtContents.setText(fromHtml(getString(R.string.nfc_info_read_contents,
                    Json.s(jsonObject, "pipe"),
                    Json.s(jsonObject, "shape"),
                    Json.s(jsonObject, "spec"),
                    Json.s(jsonObject, "unit"),
                    Json.s(jsonObject, "material"),
                    hDirection,
                    vDirection,
                    Json.s(jsonObject, "depth")
            )));
        }

        try {
            if (!jsonObject.get("spi_memo").isJsonNull()) {
                TextView txtMemo = view.findViewById(R.id.txt_memo);
                txtMemo.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                txtMemo.setText(Json.s(jsonObject, "spi_memo"));
            }

            if (imageFileUri != null) {
                ImageView imageView = view.findViewById(R.id.img_photo);
                Glide.with(view).load(imageFileUri).fitCenter().into(imageView);
            } else if (!jsonObject.get("spi_photo_url").isJsonNull()) {
                ImageView imageView = view.findViewById(R.id.img_photo);
                Glide.with(view).load(Json.s(jsonObject, "spi_photo_url")).fitCenter().into(imageView);
            }
        } catch (NullPointerException ignore) {

        }

        return view;
    }
}
