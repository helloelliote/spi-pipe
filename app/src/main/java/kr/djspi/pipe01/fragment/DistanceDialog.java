package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.helloelliote.filter.DecimalFilter;

import kr.djspi.pipe01.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.fragment.PositionDialog.fromRes;

public class DistanceDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = DistanceDialog.class.getSimpleName();
    private int positionInt = -1;
    private String dialogTitle;
    private String resId;
    private String shapeString;
    private String planString;
    private FormEditText fVertical, fHorizontal;
    private OnSelectListener listener;

    public DistanceDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            shapeString = bundle.getString("shapeString");
            positionInt = bundle.getInt("positionInt");
            planString = bundle.getString("planString");
            resId = String.format("%s_distance", planString);
        }
        dialogTitle = getString(R.string.popup_title_input_distance);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_distance, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        ImageView imageView = view.findViewById(R.id.lay_background);
        imageView.setImageDrawable(fromRes(resId));
        imageView.setScaleType(ScaleType.FIT_CENTER);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        fHorizontal = view.findViewById(R.id.form_horizontal);
        fVertical = view.findViewById(R.id.form_vertical);

        final InputFilter[] filter = {new DecimalFilter(4, 2)};
        fHorizontal.setFilters(filter);
        fVertical.setFilters(filter);

        setPosition();

        return view;
    }

    private void setPosition() {
        if (shapeString.equals("직진형")) {
            switch (positionInt) {
                case 1:
                    setTranslation(true, false, 0.0f, -50.0f, 0.0f);
                    break;
                case 2:
                    if (planString.equals("plan_plate_str_2_out") || planString.equals("plan_marker_str_2_out")) {
                        fVertical.setText("0.0");
                        fHorizontal.setText("0.0");
                        listener.onSelect(TAG_DISTANCE, 0, fHorizontal.getText().toString(), fVertical.getText().toString());
                        dismissAllowingStateLoss();
                        break;
                    } else setTranslation(false, true, -50.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    setTranslation(true, false, 0.0f, 50.0f, 0.0f);
                    break;
                case 4:
                    setTranslation(true, false, 0.0f, -100.0f, 0.0f);
                    break;
                case 5:
                    break;
                // Unreachable case
                case 6:
                    setTranslation(true, false, 0.0f, 100.0f, 0.0f);
                    break;
                case 7:
                    setTranslation(true, false, 0.0f, -50.0f, 0.0f);
                    break;
                case 8:
                    if (planString.equals("plan_plate_str_8_out") || planString.equals("plan_marker_str_8_out")) {
                        fVertical.setText("0.0");
                        fHorizontal.setText("0.0");
                        listener.onSelect(TAG_DISTANCE, 0, fHorizontal.getText().toString(), fVertical.getText().toString());
                        dismissAllowingStateLoss();
                        break;
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
                    setTranslation(false, false, -100.0f, -150.0f, -315.0f);
                    break;
                case 2:
                    setTranslation(false, true, -100.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    setTranslation(false, false, -100.0f, 155.0f, -315.0f);
                    break;
                case 4:
                    setTranslation(true, false, 0.0f, -90.0f, 0.0f);
                    break;
                case 5: // Unreachable case
                    break;
                case 6:
                    setTranslation(true, false, 0.0f, 100.0f, 0.0f);
                    break;
                case 7:
                    setTranslation(false, false, 90.0f, -150.0f, 315.0f);
                    break;
                case 8:
                    setTranslation(false, true, 95.0f, 0.0f, 0.0f);
                    break;
                case 9:
                    setTranslation(false, false, 95.0f, 155.0f, 315.0f);
                    break;
                default:
                    break;
            }
        }
    }

    private void setTranslation(boolean noV, boolean noH, float vY, float hX, float hY) {
        if (noV) {
            fVertical.setText("0.0");
            fVertical.setVisibility(GONE);
            fHorizontal.setVisibility(VISIBLE);
        }
        if (noH) {
            fHorizontal.setText("0.0");
            fHorizontal.setVisibility(GONE);
            fVertical.setVisibility(VISIBLE);
        }
        fHorizontal.setTranslationX(hX);
        fHorizontal.setTranslationY(hY);
        fVertical.setTranslationY(vY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (isAllValid()) {
                    listener.onSelect(TAG_DISTANCE, 0, fHorizontal.getText().toString(), fVertical.getText().toString());
                    fHorizontal.setVisibility(VISIBLE);
                    fVertical.setVisibility(VISIBLE);
                    dismissAllowingStateLoss();
                } else return;
                break;
            case R.id.btn_cancel:
                fHorizontal.setVisibility(VISIBLE);
                fVertical.setVisibility(VISIBLE);
                listener.onSelect(TAG_DISTANCE, -2, null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_close:
                dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    private boolean isAllValid() {
        boolean allValid = true;
        final FormEditText[] validateFields = {fHorizontal, fVertical};
        for (FormEditText field : validateFields) {
            allValid = field.testValidity() && allValid;
        }
        return allValid;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        resId = null;
        super.onDismiss(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
