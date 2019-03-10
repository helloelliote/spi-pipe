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
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;
import static kr.djspi.pipe01.RecordInputActivity.fragmentManager;

public class PositionDialog extends DialogFragment implements PlotDialog, OnClickListener {

    private static final String TAG = PositionDialog.class.getSimpleName();
    private static PlotDialog plotDialog = null;
    private static String spiTypeTag;
    private static String dialogTitle;
    private static OnSelectListener listener;
    private static int layoutResId;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static int selectIndex = -1;
    View checkView;

    public PositionDialog() {
    }

    public synchronized static PlotDialog get() {
        if (plotDialog == null) {
            plotDialog = new PositionDialog();
        }
        return plotDialog;
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
        dialogTitle = getString(R.string.popup_title_select_position);
        switch (spiTypeTag) {
            case TAG_TYPE_PLATE:
                layoutResId = R.layout.fragment_plot_plate;
                break;
            case TAG_TYPE_MARKER:
                layoutResId = R.layout.fragment_plot_marker;
                break;
            case TAG_TYPE_COLUMN:
                layoutResId = R.layout.fragment_plot_column;
                break;
            default:
                break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        view.findViewById(R.id.lay_1).setOnClickListener(this);
        view.findViewById(R.id.lay_2).setOnClickListener(this);
        view.findViewById(R.id.lay_3).setOnClickListener(this);
        view.findViewById(R.id.lay_4).setOnClickListener(this);
        view.findViewById(R.id.lay_5).setOnClickListener(this);
        view.findViewById(R.id.lay_6).setOnClickListener(this);
        view.findViewById(R.id.lay_7).setOnClickListener(this);
        view.findViewById(R.id.lay_8).setOnClickListener(this);
        view.findViewById(R.id.lay_9).setOnClickListener(this);
        checkView = view.findViewById(R.id.v_select);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (selectIndex == -1) {
                    Toast.makeText(getContext(), "관로의 위치를 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(TAG_POSITION, selectIndex);
                PlotDialog plotDialog = DirectionDialog.get();
                plotDialog.show(fragmentManager, spiTypeTag);
                selectIndex = -1;
                dismissAllowingStateLoss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
                selectIndex = -1;
                dismissAllowingStateLoss();
                break;
            case R.id.lay_1:
                selectIndex = 1;
                setFocus(v);
                break;
            case R.id.lay_2:
                selectIndex = 2;
                setFocus(v);
                break;
            case R.id.lay_3:
                selectIndex = 3;
                setFocus(v);
                break;
            case R.id.lay_4:
                selectIndex = 4;
                setFocus(v);
                break;
            case R.id.lay_5:
                selectIndex = 5;
                setFocus(v);
                break;
            case R.id.lay_6:
                selectIndex = 6;
                setFocus(v);
                break;
            case R.id.lay_7:
                selectIndex = 7;
                setFocus(v);
                break;
            case R.id.lay_8:
                selectIndex = 8;
                setFocus(v);
                break;
            case R.id.lay_9:
                selectIndex = 9;
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
