package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.TAG_TYPE_C;
import static kr.djspi.pipe01.Const.TAG_TYPE_M;
import static kr.djspi.pipe01.Const.TAG_TYPE_P;

public class PlotDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = ListDialog.class.getSimpleName();
    private static String listTag;
    private static String listTitle;
    private static OnSelectListener listener;
    private static int layoutResId;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static int selectIndex = -1;

    private static class LazyHolder {
        static final ListDialog INSTANCE = new ListDialog();
    }

    @Contract(pure = true)
    public static ListDialog get() {
        return LazyHolder.INSTANCE;
    }

    public PlotDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listTag = getTag();
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listTitle = getString(R.string.popup_title_select_position);
        switch (listTag) {
            case TAG_TYPE_P:
                layoutResId = R.layout.fragment_plotselect_p;
                break;
            case TAG_TYPE_M:
                layoutResId = R.layout.fragment_plotselect_m;
                break;
            case TAG_TYPE_C:
                layoutResId = R.layout.fragment_plotselect_c;
                break;
            default:
                break;
        }
    }

    /**
     * 레이아웃 구성 및 기능 초기화
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(listTitle);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                listener.onSelect(listTag, selectIndex);
                selectIndex = -1;
                dismiss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
                dismiss();
                dismiss();
            default:
                break;
        }
    }
}
