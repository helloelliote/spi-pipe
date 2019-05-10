package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.RESULT_FAIL;
import static kr.djspi.pipe01.Const.TAG_LOCATION;

public class LocationDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = LocationDialog.class.getSimpleName();
    private int selectIndex = -1;
    private String dialogTitle;
    private OnSelectListener listener;

    public LocationDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
        selectIndex = -1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogTitle = getString(R.string.popup_title_location);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        view.findViewById(R.id.btn_survey).setOnClickListener(this);
        view.findViewById(R.id.btn_gps).setOnClickListener(this);
        view.findViewById(R.id.btn_dismiss).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_survey:
                selectIndex = 1;
                listener.onSelect(TAG_LOCATION, selectIndex, (String) null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_gps:
                selectIndex = 2;
                listener.onSelect(TAG_LOCATION, selectIndex, (String) null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_dismiss:
                listener.onSelect(TAG_LOCATION, RESULT_FAIL, (String) null);
                dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
