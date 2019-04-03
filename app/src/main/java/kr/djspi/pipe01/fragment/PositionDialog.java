package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kr.djspi.pipe01.R;

import static android.view.Gravity.CENTER;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static kr.djspi.pipe01.BaseActivity.packageName;
import static kr.djspi.pipe01.BaseActivity.resources;
import static kr.djspi.pipe01.Const.PIPE_SHAPES;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;

public class PositionDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = PositionDialog.class.getSimpleName();
    private int selectIndex = -1;
    private String typeString;
    private String dialogTitle;
    private Bundle bundle;
    private String shapeString;
    private View checkView;
    private FragmentManager fragmentManager;
    private OnSelectListener listener;

    public PositionDialog() {
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
        fragmentManager = getFragmentManager();
        if (getArguments() != null) {
            bundle = getArguments();
            typeString = bundle.getString("typeString");
            shapeString = bundle.getString("shapeString");
        }
        dialogTitle = getString(R.string.popup_title_select_position);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_position, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

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

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        setLayoutVisibility(view);

        return view;
    }

    private void setLayoutVisibility(@NotNull View view) {
        ImageView backgroundImage = view.findViewById(R.id.lay_background);
        final String defType = "id";
        ImageView[] views = new ImageView[10];
        for (int i = 1; i <= 9; i++) {
            views[i] = view.findViewById(resources.getIdentifier("image_" + i, defType, packageName));
        }
        switch (typeString) {
            case TAG_TYPE_PLATE:
                backgroundImage.setImageDrawable(fromRes(R.drawable.bg_p));
                view.findViewById(R.id.lay_row_2).setVisibility(INVISIBLE);
                views[1].setImageDrawable(fromRes(R.drawable.btn_01_7));
                views[2].setImageDrawable(fromRes(R.drawable.btn_01_8));
                views[3].setImageDrawable(fromRes(R.drawable.btn_01_9));
                views[7].setImageDrawable(fromRes(R.drawable.btn_01_1));
                views[8].setImageDrawable(fromRes(R.drawable.btn_01_2));
                views[9].setImageDrawable(fromRes(R.drawable.btn_01_3));
                break;
            case TAG_TYPE_MARKER:
                backgroundImage.setImageDrawable(fromRes(R.drawable.bg_m));
                view.findViewById(R.id.lay_row_3).setVisibility(GONE);
                views[1].setImageDrawable(fromRes(R.drawable.btn_10_7));
                views[2].setImageDrawable(fromRes(R.drawable.btn_10_8));
                views[3].setImageDrawable(fromRes(R.drawable.btn_10_9));
                views[5].setImageDrawable(fromRes(R.drawable.btn_10_2));
                break;
            case TAG_TYPE_COLUMN:
                LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER);
                params.setMargins(0, 60, 0, 0);
                view.findViewById(R.id.lay_rows).setLayoutParams(params);
                backgroundImage.setImageDrawable(fromRes(R.drawable.bg_c_2));
                views[1].setImageDrawable(fromRes(R.drawable.btn_11_7));
                views[2].setImageDrawable(fromRes(R.drawable.btn_11_8));
                views[3].setImageDrawable(fromRes(R.drawable.btn_11_9));
                views[4].setImageDrawable(fromRes(R.drawable.btn_11_4));
                views[5].setImageDrawable(fromRes(R.drawable.btn_11_5));
                views[6].setImageDrawable(fromRes(R.drawable.btn_11_6));
                views[7].setImageDrawable(fromRes(R.drawable.btn_11_1));
                views[8].setImageDrawable(fromRes(R.drawable.btn_11_2));
                views[9].setImageDrawable(fromRes(R.drawable.btn_11_3));
                if (shapeString.equals(PIPE_SHAPES[0])) { // 직진형
                    views[1].setVisibility(GONE);
                    ((View) views[1].getParent()).setVisibility(GONE);
                    views[3].setVisibility(GONE);
                    ((View) views[3].getParent()).setVisibility(GONE);
                    views[7].setVisibility(GONE);
                    ((View) views[7].getParent()).setVisibility(GONE);
                    views[9].setVisibility(GONE);
                    ((View) views[9].getParent()).setVisibility(GONE);
                }
                break;
            default:
                break;
        }
    }

    static Drawable fromRes(String resId) {
        return resources.getDrawable(resources.getIdentifier(resId, "drawable", packageName), null);
    }

    private static Drawable fromRes(@DrawableRes int resId) {
        return resources.getDrawable(resId, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (selectIndex == -1) {
                    Toast.makeText(getContext(), "관로의 위치를 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(TAG_POSITION, selectIndex, (String) null);
                DirectionDialog dialog = new DirectionDialog();
                bundle.putInt("positionInt", selectIndex);
                dialog.setArguments(bundle);
                dialog.show(fragmentManager, TAG_DIRECTION);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
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

    private void setFocus(@NotNull View view) {
        checkView.setVisibility(INVISIBLE);
        view.findViewById(R.id.v_select).setVisibility(VISIBLE);
        this.checkView = view.findViewById(R.id.v_select);
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
