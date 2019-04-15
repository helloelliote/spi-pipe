package kr.djspi.pipe01.tab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;

import kr.djspi.pipe01.R;

import static android.view.View.GONE;
import static kr.djspi.pipe01.BaseActivity.packageName;
import static kr.djspi.pipe01.BaseActivity.screenScale;

public class SectionTab extends Fragment {

    private static final String TAG = SectionTab.class.getSimpleName();
    private JsonObject jsonObject;
    private TextView tVertical, tDepth, tSpec, tMaterial;

    public SectionTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            OnRecordListener listener = (OnRecordListener) context;
            jsonObject = listener.getJsonObject();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_section, container, false);
        try {
            String resId = Json.s(jsonObject, "file_section").replace(".png", "");
            ImageView imageView = view.findViewById(R.id.planeImageView);
            imageView.setImageResource(getResources().getIdentifier(resId, "drawable", packageName));
        } catch (UnsupportedOperationException | NullPointerException e) {
            view.findViewById(R.id.lay_0).setVisibility(GONE);
        }

        tVertical = view.findViewById(R.id.text_vertical);
        tDepth = view.findViewById(R.id.text_depth);
        tVertical.setText(Json.s(jsonObject, "vertical"));
        tDepth.setText(Json.s(jsonObject, "depth"));

        tSpec = view.findViewById(R.id.text_spec);
        tMaterial = view.findViewById(R.id.text_material);
        tSpec.setText(String.format("%s %s %s",
                Json.s(jsonObject, "header"),
                Json.s(jsonObject, "spec"),
                Json.s(jsonObject, "unit")));
        tMaterial.setText(Json.s(jsonObject, "material"));

        setPosition();

        return view;
    }

    private void setPosition() {
        final int positionInt = Json.i(jsonObject, "position");
        switch (positionInt) {
            case 1:
            case 2:
            case 3:
                setTranslation(false, -355.0f);
                break;
            case 4:
            case 5:
            case 6:
                tSpec.setTranslationX(175.0f);
                tMaterial.setTranslationX(175.0f);
                setTranslation(true, 0.0f);
                break;
            case 7:
            case 8:
            case 9:
                setTranslation(false, 355.0f);
                break;
            default:
                break;
        }
    }

    private void setTranslation(boolean noV, float dX) {
        if (noV) {
            tVertical.setVisibility(GONE);
        }
        tDepth.setTranslationX(dX * screenScale);
        tDepth.setTranslationY(77.5f * screenScale);
        tVertical.setTranslationY(-475.0f * screenScale);
        tSpec.setTranslationY(300.0f * screenScale);
        tMaterial.setTranslationY(400.0f * screenScale);
    }
}
