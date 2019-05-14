package kr.djspi.pipe01.tab;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.SpiPhotoObject;
import kr.djspi.pipe01.fragment.ImageDialog;

import static android.text.Html.fromHtml;
import static kr.djspi.pipe01.Const.TAG_PHOTO;

public class InfoTab extends Fragment {

    private static final String TAG = InfoTab.class.getSimpleName();
    private JsonObject jsonObject;
    private Uri imageUri;

    public InfoTab() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            OnRecordListener listener = (OnRecordListener) context;
            jsonObject = listener.getJsonObject();
            imageUri = listener.getUri();
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
                hDirection = Json.s(jsonObject, "spi_type").equals("표지주") ?
                        String.format("차도반대측 방향 %s m", Json.s(jsonObject, "vertical")) :
                        String.format("보도 방향 %s m", Json.s(jsonObject, "vertical"));
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
        } catch (NullPointerException ignore) {
        }

        try {
            ImageView imageView = view.findViewById(R.id.img_photo);
            RequestBuilder<Drawable> requestBuilder = null;
            SpiPhotoObject photoObj = new SpiPhotoObject();
            if (imageUri != null) {
                requestBuilder = Glide.with(view).load(imageUri);
                photoObj.setUri(imageUri);
            } else {
                if (!jsonObject.get("spi_photo_url").isJsonNull()) {
                    requestBuilder = Glide.with(view).load(Json.s(jsonObject, "spi_photo_url"));
                    photoObj.setUrl(Json.s(jsonObject, "spi_photo_url"));
                }
            }
            if (requestBuilder != null) {
                requestBuilder.fitCenter()
                        .error(R.drawable.ic_photo_error)
                        .dontAnimate()
                        .into(imageView);
                imageView.setOnClickListener(v -> {
                    ImageDialog imageDialog = new ImageDialog();
                    Bundle bundle = new Bundle(1);
                    bundle.putSerializable("SpiPhotoObject", photoObj);
                    imageDialog.setArguments(bundle);
                    imageDialog.show(getFragmentManager(), TAG_PHOTO);
                });
            }
        } catch (NullPointerException ignore) {
        }

        return view;
    }
}
