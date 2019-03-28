package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.RecordInputActivity2.showPositionDialog;
import static kr.djspi.pipe01.fragment.PositionDialog.fromRes;

public class DistanceDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = DistanceDialog.class.getSimpleName();
    private static String dialogTitle;
    private static String resId;
    private static Bundle bundle;
    private static FormEditText fVertical, fHorizontal;
    private static OnSelectListener listener;

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
            bundle = getArguments();
            resId = String.format("%s_distance", bundle.getString("planString"));
        }
        dialogTitle = getString(R.string.popup_title_input_distance);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_distance, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        ImageView imageView = view.findViewById(R.id.lay_background);
        // TODO: 2019-03-28 도면 세팅 완료 후 정리하기
        try {
            imageView.setImageDrawable(fromRes(resId));
        } catch (Exception e) {
            imageView.setImageDrawable(fromRes(bundle.getString("planString")));
        }
        imageView.setScaleType(ScaleType.FIT_CENTER);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        fHorizontal = view.findViewById(R.id.form_horizontal);
        fVertical = view.findViewById(R.id.form_vertical);

        final InputFilter[] filter = {new DecimalFilter(4, 2)};
        fHorizontal.setFilters(filter);
        fVertical.setFilters(filter);

        switch (bundle.getInt("positionInt")) {
            case 1:
                setTranslation(null, -110.0f, -150.0f, -315.0f);
                break;
            case 2:
                setTranslation(fHorizontal, -110.0f, 0.0f, 0.0f);
                break;
            case 3:
                setTranslation(null, -110.0f, 150.0f, -315.0f);
                break;
            case 4:
                setTranslation(fVertical, 0.0f, 0.0f, 0.0f);
                break;
            case 5:
                // Unreachable case
                break;
            case 6:
                setTranslation(fVertical, 0.0f, 0.0f, 0.0f);
                break;
            case 7:
                setTranslation(null, 110.0f, -150.0f, 315.0f);
                break;
            case 8:
                setTranslation(fHorizontal, 110.0f, 0.0f, 0.0f);
                break;
            case 9:
                setTranslation(null, 110.0f, 150.0f, 315.0f);
                break;
            default:
                break;
        }

        return view;
    }

    private static void setTranslation(@Nullable FormEditText disable, float vY, float hX, float hY) {
        if (disable != null) {
            disable.setText("0.0");
            disable.setVisibility(GONE);
            disable.setEnabled(false);
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
                    listener.onSelect(TAG_DISTANCE, 0,
                            fHorizontal.getText().toString(),
                            fVertical.getText().toString());
                    dismissAllowingStateLoss();
                } else return;
                break;
            case R.id.btn_cancel:
                dismissAllowingStateLoss();
                showPositionDialog();
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
}
