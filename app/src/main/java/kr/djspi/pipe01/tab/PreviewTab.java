package kr.djspi.pipe01.tab;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.RESULT_FAIL;
import static kr.djspi.pipe01.Const.RESULT_PASS;
import static kr.djspi.pipe01.Const.TAG_PREVIEW;

public class PreviewTab extends Fragment implements OnClickListener {

    private static final String TAG = PreviewTab.class.getSimpleName();
    private JsonObject jsonObject;
    private Uri imageFileUri;
    private OnRecordListener listener;

    public PreviewTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            listener = (OnRecordListener) context;
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
        View view = inflater.inflate(R.layout.tab_preview, container, false);

        TextView tPipe = view.findViewById(R.id.text_pipe);
        tPipe.setText(Json.s(jsonObject, "pipe"));

        TextView tShape = view.findViewById(R.id.text_shape);
        tShape.setText(Json.s(jsonObject, "shape"));

        TextView tHorizontal = view.findViewById(R.id.text_horizontal);
        tHorizontal.setText(Json.s(jsonObject, "horizontal_form"));

        TextView tVertical = view.findViewById(R.id.text_vertical);
        tVertical.setText(Json.s(jsonObject, "vertical_form"));

        TextView tDepth = view.findViewById(R.id.text_depth);
        tDepth.setText(Json.s(jsonObject, "depth"));

        TextView tHeader = view.findViewById(R.id.header);
        tHeader.setText(Json.s(jsonObject, "header"));

        TextView tSpec = view.findViewById(R.id.text_spec);
        tSpec.setText(Json.s(jsonObject, "spec"));

        TextView tUnit = view.findViewById(R.id.unit);
        tUnit.setText(Json.s(jsonObject, "unit"));

        TextView tMaterial = view.findViewById(R.id.text_material);
        tMaterial.setText(Json.s(jsonObject, "material"));

        TextView tSupervise = view.findViewById(R.id.text_supervise);
        tSupervise.setText(Json.s(jsonObject, "supervise"));

        TextView tSuperviseContact = view.findViewById(R.id.text_supervise_contact);
        tSuperviseContact.setText(Json.s(jsonObject, "supervise_contact"));

        TextView tMemo = view.findViewById(R.id.text_memo);
        tMemo.setText(Json.s(jsonObject, "spi_memo"));

        TextView tConstruction = view.findViewById(R.id.text_construction);
        tConstruction.setText(Json.s(jsonObject, "construction"));

        TextView tConstructionContact = view.findViewById(R.id.text_construction_contact);
        tConstructionContact.setText(Json.s(jsonObject, "construction_contact"));

        TextView tPhoto = view.findViewById(R.id.text_photo);
        if (imageFileUri != null) {
            tPhoto.setText(getString(R.string.record_photo_ok));
        } else tPhoto.setText(null);

        view.findViewById(R.id.button_confirm).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_confirm:
                listener.onRecord(TAG_PREVIEW, RESULT_PASS);
                break;
            default:
                listener.onRecord(TAG_PREVIEW, RESULT_FAIL);
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
