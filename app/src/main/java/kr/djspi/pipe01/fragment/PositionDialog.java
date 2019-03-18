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
import static kr.djspi.pipe01.BaseActivity.resources;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;
import static kr.djspi.pipe01.RecordInputActivity.fragmentManager;

public class PositionDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = PositionDialog.class.getSimpleName();
    private static int selectIndex = -1;
    private static String typeString;
    private static String dialogTitle;
    private static Bundle bundle;
    private static OnSelectListener listener;
    private View checkView;

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
        if (getArguments() != null) {
            bundle = getArguments();
            typeString = bundle.getString("typeString");
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
        View[] views = new View[10];
        for (int i = 1; i <= 9; i++) {
            views[i] = view.findViewById(resources.getIdentifier("image_" + i, defType, getContext().getPackageName()));
        }
        switch (typeString) {
            case TAG_TYPE_PLATE:
                backgroundImage.setImageDrawable(resources.getDrawable(R.drawable.bg_p, null));
                view.findViewById(R.id.lay_row_2).setVisibility(INVISIBLE);
                views[1].setBackgroundResource(R.drawable.btn_01_7);
                views[2].setBackgroundResource(R.drawable.btn_01_8);
                views[3].setBackgroundResource(R.drawable.btn_01_9);
                views[7].setBackgroundResource(R.drawable.btn_01_1);
                views[8].setBackgroundResource(R.drawable.btn_01_2);
                views[9].setBackgroundResource(R.drawable.btn_01_3);
                break;
            case TAG_TYPE_MARKER:
                backgroundImage.setImageDrawable(resources.getDrawable(R.drawable.bg_m, null));
                view.findViewById(R.id.lay_row_3).setVisibility(GONE);
                views[1].setBackgroundResource(R.drawable.btn_10_7);
                views[2].setBackgroundResource(R.drawable.btn_10_8);
                views[3].setBackgroundResource(R.drawable.btn_10_9);
                views[5].setBackgroundResource(R.drawable.btn_10_2);
                break;
            case TAG_TYPE_COLUMN:
                LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER);
                params.setMargins(0, 60, 0, 0);
                view.findViewById(R.id.lay_rows).setLayoutParams(params);
                backgroundImage.setImageDrawable(resources.getDrawable(R.drawable.bg_c_2, null));
                views[1].setBackgroundResource(R.drawable.btn_11_7);
                views[2].setBackgroundResource(R.drawable.btn_11_8);
                views[3].setBackgroundResource(R.drawable.btn_11_9);
                views[4].setBackgroundResource(R.drawable.btn_11_4);
                views[5].setBackgroundResource(R.drawable.btn_11_5);
                views[6].setBackgroundResource(R.drawable.btn_11_6);
                views[7].setBackgroundResource(R.drawable.btn_11_1);
                views[8].setBackgroundResource(R.drawable.btn_11_2);
                views[9].setBackgroundResource(R.drawable.btn_11_3);
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
                    Toast.makeText(getContext(), "관로의 위치를 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(TAG_POSITION, selectIndex, null);
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
