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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_POSITION;

public class DirectionDialog extends DialogFragment implements PlotDialog, OnSelectListener, OnClickListener {

    private static final String TAG = DirectionDialog.class.getSimpleName();
    private static PlotDialog plotDialog = null;
    private static String spiTypeTag;
    private static String dialogTitle;
    private static OnSelectListener listener;
    private static int layoutResId;
    private FrameLayout mLayImg1;
    private FrameLayout mLayImg2;
    private FrameLayout mLayImg3;
    private FrameLayout mLayImg4;
    private ImageView mImgPlan1;
    private ImageView mImgPlan2;
    private ImageView mImgPlan3;
    private ImageView mImgPlan4;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static int selectIndex = -1;
    View checkView;

    public DirectionDialog() {
    }

    public synchronized static PlotDialog get() {
        if (plotDialog == null) {
            plotDialog = new DirectionDialog();
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
        dialogTitle = getString(R.string.popup_title_select_direction);
        layoutResId = R.layout.fragment_plot_direction;
        // TODO: 2019-03-09 resId 생성
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        mLayImg1 = view.findViewById(R.id.lay_img_1);
        mLayImg2 = view.findViewById(R.id.lay_img_2);
        mLayImg3 = view.findViewById(R.id.lay_img_3);
        mLayImg4 = view.findViewById(R.id.lay_img_4);
        mLayImg1.setOnClickListener(this);
        mLayImg2.setOnClickListener(this);
        mLayImg3.setOnClickListener(this);
        mLayImg4.setOnClickListener(this);
        mImgPlan1 = view.findViewById(R.id.img_1);
        mImgPlan2 = view.findViewById(R.id.img_2);
        mImgPlan3 = view.findViewById(R.id.img_3);
        mImgPlan4 = view.findViewById(R.id.img_4);
        checkView = view.findViewById(R.id.v_select);

        return view;
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
            case R.id.lay_img_1:
                selectIndex = 2;
                setFocus(v);
                break;
            case R.id.lay_img_2:
                selectIndex = 8;
                setFocus(v);
                break;
            case R.id.lay_img_3:
                selectIndex = 4;
                setFocus(v);
                break;
            case R.id.lay_img_4:
                selectIndex = 6;
                setFocus(v);
                break;
            case R.id.btn_cancel:
                selectIndex = -1;
            case R.id.btn_close:
                dismissAllowingStateLoss();
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
    public void onSelect(String tag, int index) {
        if (index == -1) return;
        switch (tag) {
            case TAG_POSITION:
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
