package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;

public class DirectionDialog extends DialogFragment implements PlotDialog, OnClickListener {

    private static final String TAG = DirectionDialog.class.getSimpleName();
    private static PlotDialog directionDialog = null;
    private static String spiTypeTag;
    private static String dialogTitle;
    private static int selectIndex = -1;
    private static OnSelectListener listener;
    private View checkView;

    public DirectionDialog() {
    }

    public synchronized static PlotDialog get() {
        if (directionDialog == null) {
            directionDialog = new DirectionDialog();
        }
        return directionDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        spiTypeTag = getTag();
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogTitle = getString(R.string.popup_title_select_direction);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_direction, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        view.findViewById(R.id.lay_1).setOnClickListener(this);
        view.findViewById(R.id.lay_2).setOnClickListener(this);
        view.findViewById(R.id.lay_3).setOnClickListener(this);
        view.findViewById(R.id.lay_4).setOnClickListener(this);

        checkView = view.findViewById(R.id.v_select);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        setLayoutVisibility(view);

        return view;
    }

    private void setLayoutVisibility(View view) {
        ImageView image_1 = view.findViewById(R.id.image_1);
        ImageView image_2 = view.findViewById(R.id.image_2);
        ImageView image_3 = view.findViewById(R.id.image_3);
        ImageView image_4 = view.findViewById(R.id.image_4);
        switch (spiTypeTag) {
            case TAG_TYPE_PLATE:
//                image_1.setBackgroundResource(R.id.);
                break;
            case TAG_TYPE_MARKER:

                break;
            case TAG_TYPE_COLUMN:

                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (selectIndex == -1) {
                    Toast.makeText(getContext(), "관로의 방향을 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(TAG_DIRECTION, selectIndex);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
                selectIndex = -1;
                dismissAllowingStateLoss();
                break;
            case R.id.lay_1:
                selectIndex = 2;
                setFocus(v);
                break;
            case R.id.lay_2:
                selectIndex = 8;
                setFocus(v);
                break;
            case R.id.lay_3:
                selectIndex = 4;
                setFocus(v);
                break;
            case R.id.lay_4:
                selectIndex = 6;
                setFocus(v);
                break;
            default:
                break;
        }
    }

    final void setFocus(@NotNull View view) {
        checkView.setVisibility(INVISIBLE);
        view.findViewById(R.id.v_select).setVisibility(VISIBLE);
        this.checkView = view.findViewById(R.id.v_select);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        selectIndex = -1;
        super.onDismiss(dialog);
    }
}
