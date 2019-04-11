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
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.BaseActivity.packageName;
import static kr.djspi.pipe01.BaseActivity.screenScale;

public class PlaneTab extends Fragment {

    private static final String TAG = PlaneTab.class.getSimpleName();
    private JsonObject jsonObject;
    private TextView tHorizontal, tVertical;
    private String resId;

    public PlaneTab() {
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
        View view = inflater.inflate(R.layout.tab_plane, container, false);
        try {
            resId = Json.s(jsonObject, "file_plane").replace(".png", "_distance");
            ImageView imageView = view.findViewById(R.id.planeImageView);
            imageView.setImageResource(getResources().getIdentifier(resId, "drawable", packageName));
        } catch (UnsupportedOperationException | NullPointerException e) {
            view.findViewById(R.id.lay_0).setVisibility(GONE);
        }

        tHorizontal = view.findViewById(R.id.text_horizontal);
        tVertical = view.findViewById(R.id.text_vertical);
        tHorizontal.setText(Json.s(jsonObject, "horizontal"));
        tVertical.setText(Json.s(jsonObject, "vertical"));

        setPosition();

        return view;
    }

    private void setPosition() {
        // TODO: 2019-04-11 직상일때 0.00 칸 중간에 나오는 문제
        final int positionInt = Json.i(jsonObject, "position");
        if (Json.s(jsonObject, "shape").equals("직진형")) {
            switch (positionInt) {
                case 1:
                    setTranslation(true, false, 0.0f, -50.0f, 0.0f);
                    break;
                case 2:
                    if (resId.equals("plan_plate_str_2_out_distance")) {
                        setTranslation(true, true, 0.0f, 0.0f, 0.0f);
                    } else setTranslation(false, true, -50.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    setTranslation(true, false, 0.0f, 50.0f, 0.0f);
                    break;
                case 4:
                    setTranslation(true, false, 0.0f, -100.0f, 0.0f);
                    break;
                case 5:
                    tHorizontal.setVisibility(GONE);
                    tVertical.setVisibility(GONE);
                    break;
                case 6:
                    setTranslation(true, false, 0.0f, 100.0f, 0.0f);
                    break;
                case 7:
                    setTranslation(true, false, 0.0f, -50.0f, 0.0f);
                    break;
                case 8:
                    if (resId.equals("plan_plate_str_8_out_distance")) {
                        setTranslation(true, true, 0.0f, 0.0f, 0.0f);
                    } else setTranslation(false, true, 50.0f, 0.0f, 0.0f);
                    break;
                case 9:
                    setTranslation(true, false, 0.0f, 50.0f, 0.0f);
                    break;
                default:
                    break;
            }
        } else {
            switch (positionInt) {
                case 1:
                    setTranslation(false, false, -100.0f, -170.0f, -350.0f);
                    break;
                case 2:
                    setTranslation(false, true, -100.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    setTranslation(false, false, -100.0f, 175.0f, -350.0f);
                    break;
                case 4:
                    setTranslation(true, false, 0.0f, -90.0f, 0.0f);
                    break;
                case 5:
                    tHorizontal.setVisibility(GONE);
                    tVertical.setVisibility(GONE);
                    break;
                case 6:
                    setTranslation(true, false, 0.0f, 100.0f, 0.0f);
                    break;
                case 7:
                    setTranslation(false, false, 90.0f, -170.0f, 350.0f);
                    break;
                case 8:
                    setTranslation(false, true, 95.0f, 0.0f, 0.0f);
                    break;
                case 9:
                    setTranslation(false, false, 95.0f, 175.0f, 350.0f);
                    break;
                default:
                    break;
            }
        }
    }

    private void setTranslation(boolean noV, boolean noH, float vY, float hX, float hY) {
        if (noV) {
            tVertical.setVisibility(GONE);
            tHorizontal.setVisibility(VISIBLE);
        }
        if (noH) {
            tHorizontal.setVisibility(GONE);
            tVertical.setVisibility(VISIBLE);
        }
        tHorizontal.setTranslationX(hX * screenScale);
        tHorizontal.setTranslationY(hY * screenScale);
        tVertical.setTranslationY(vY * screenScale);
    }
}
