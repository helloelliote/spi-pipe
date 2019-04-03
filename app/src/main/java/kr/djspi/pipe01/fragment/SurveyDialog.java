package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.helloelliote.filter.DecimalFilter;
import com.helloelliote.geolocation.GeoTrans;

import kr.djspi.pipe01.R;

import static java.util.Objects.requireNonNull;
import static kr.djspi.pipe01.Const.RESULT_FAIL;
import static kr.djspi.pipe01.Const.RESULT_PASS;
import static kr.djspi.pipe01.Const.TAG_SURVEY;

public class SurveyDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = SurveyDialog.class.getSimpleName();
    private static final double INPUT_LIMIT = 999999.9999; // 최대 입력값
    public static GeoTrans.Coordinate originPoint;
    private final InputFilter[] FILTER_SURVEY = {new DecimalFilter(10, 4)};
    private int selectIndex = -1;
    private String dialogTitle;
    private RadioGroup radioGroup; // 원점 선택 라디오 버튼 그룹
    private TextInputLayout inputLayout_x, inputLayout_y;
    private TextInputEditText input_x, input_y;
    private OnSelectListener listener;

    public SurveyDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogTitle = getString(R.string.popup_title_survey);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_survey, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        radioGroup = view.findViewById(R.id.nmap_radiogroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = radioGroup.findViewById(checkedId);
            selectIndex = radioGroup.indexOfChild(checkedRadioButton);
            switch (checkedId) {
                case R.id.nmap_radio_central:
                    originPoint = GeoTrans.Coordinate.GRS80_MIDDLE_WITH_JEJUDO;
                    break;
                case R.id.nmap_radio_east:
                    originPoint = GeoTrans.Coordinate.GRS80_EAST;
                    break;
                case R.id.nmap_radio_eastsea:
                    originPoint = GeoTrans.Coordinate.GRS80_EASTSEA;
                    break;
                case R.id.nmap_radio_west:
                    originPoint = GeoTrans.Coordinate.GRS80_WEST;
                    break;
                default:
                    break;
            }
        });
        inputLayout_x = view.findViewById(R.id.lay_coordinate_x);
        inputLayout_y = view.findViewById(R.id.lay_coordinate_y);
        input_x = inputLayout_x.findViewById(R.id.input_coordinate_x);
        input_y = inputLayout_y.findViewById(R.id.input_coordinate_y);
        input_x.setFilters(FILTER_SURVEY);
        input_y.setFilters(FILTER_SURVEY);

        view.findViewById(R.id.btn_dismiss).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (isInputValid(input_x, input_y)) {
                    assert input_x.getText() != null;
                    assert input_y.getText() != null;
                    listener.onSelect(TAG_SURVEY, RESULT_PASS,
                            input_x.getText().toString(),
                            input_y.getText().toString());
                    dismissAllowingStateLoss();
                } else selectIndex = -1;
                break;
            case R.id.btn_dismiss:
                listener.onSelect(TAG_SURVEY, RESULT_FAIL, (String) null);
                dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    /**
     * 사용자의 입력값이 유효한 값인지 검사: Null 체크, 유효범위 체크
     *
     * @param input_x
     * @param input_y
     * @return boolean isX, boolean isY 입력값 X AND Y 가 유효하면 true 리턴
     */
    private boolean isInputValid(TextInputEditText input_x, TextInputEditText input_y) {
        boolean isX = false;
        boolean isY = false;

        try {
            double value_x = Double.valueOf(requireNonNull(input_x.getText()).toString());
            if (value_x > INPUT_LIMIT) {
                inputLayout_x.setError(getString(R.string.map_coord_error));
            } else {
                isX = true;
                inputLayout_x.setError(null);
                input_x.clearFocus();
            }
        } catch (NullPointerException | NumberFormatException e) {
            inputLayout_x.setError(getString(R.string.map_input_error));
        }

        try {
            double value_y = Double.valueOf(requireNonNull(input_y.getText()).toString());
            if (value_y > INPUT_LIMIT) {
                inputLayout_y.setError(getString(R.string.map_coord_error));
            } else {
                isY = true;
                inputLayout_y.setError(null);
                input_y.clearFocus();
            }
        } catch (NullPointerException | NumberFormatException e) {
            inputLayout_y.setError(getString(R.string.map_input_error));
        }
        return (isX && isY);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        selectIndex = -1;
        super.onDismiss(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
