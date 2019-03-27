package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.RecordInputActivity2.showPositionDialog;
import static kr.djspi.pipe01.fragment.PositionDialog.fromRes;

public class DistanceDialog extends DialogFragment implements OnSelectListener, View.OnClickListener {

    private static final String TAG = DistanceDialog.class.getSimpleName();
    private static String dialogTitle;
    private static String resId;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            resId = bundle.getString("planString");
        }
        dialogTitle = getString(R.string.popup_title_input_distance);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_distance, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        ImageView imageView = view.findViewById(R.id.lay_background);
        imageView.setImageDrawable(fromRes(resId));
        imageView.setScaleType(ScaleType.FIT_CENTER);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
//                if (selectIndex == -1) {
//                    Toast.makeText(getContext(), "관로의 방향을 선택해주세요", Toast.LENGTH_LONG).show();
//                    return;
//                }
                listener.onSelect(TAG_DISTANCE, -1, "");
                dismissAllowingStateLoss();
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

    @Override
    public void onSelect(String tag, int index, String text) {
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        resId = null;
        super.onDismiss(dialog);
    }
}
