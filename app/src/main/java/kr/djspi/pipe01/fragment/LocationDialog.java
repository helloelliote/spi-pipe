package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.TAG_LOCATION;

public class LocationDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = LocationDialog.class.getSimpleName();
    private static String dialogTitle;
    private static int selectIndex = -1;
    private static OnSelectListener listener;

    public LocationDialog() {
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

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_survey:
                selectIndex = 1;
                listener.onSelect(TAG_LOCATION, selectIndex, null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_gps:
                selectIndex = 2;
                listener.onSelect(TAG_LOCATION, selectIndex, null);
                dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        selectIndex = -1;
        super.onDismiss(dialog);
    }
}
