package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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
import static kr.djspi.pipe01.BaseActivity.resources;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.RecordInputActivity.showPositionDialog;
import static kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.parsePipeShape;
import static kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.parseSpiType;

public class DirectionDialog extends DialogFragment implements OnSelectListener, OnClickListener {

    private static final String TAG = DirectionDialog.class.getSimpleName();
    private static String dialogTitle;
    private static String typeString;
    private static String shapeString;
    private static String[] resIds;
    private static int selectIndex = -1;
    private static int positionIndex = -1;
    private static OnSelectListener listener;
    private View checkView;

    public DirectionDialog() {
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
            Bundle bundle = getArguments();
            typeString = bundle.getString("typeString");
            shapeString = bundle.getString("shapeString");
            positionIndex = bundle.getInt("positionInt");
        }
        dialogTitle = getString(R.string.popup_title_select_direction);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot_direction, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        view.findViewById(R.id.lay_2).setOnClickListener(this);
        view.findViewById(R.id.lay_4).setOnClickListener(this);
        view.findViewById(R.id.lay_6).setOnClickListener(this);
        view.findViewById(R.id.lay_8).setOnClickListener(this);

        checkView = view.findViewById(R.id.v_select);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        setLayoutVisibility(view);

        return view;
    }

    private void setLayoutVisibility(@NotNull View view) {
        ImageView image_2 = view.findViewById(R.id.image_2);
        ImageView image_8 = view.findViewById(R.id.image_8);
        ImageView image_4 = view.findViewById(R.id.image_4);
        ImageView image_6 = view.findViewById(R.id.image_6);

        resIds = new String[10];
        for (int i = 1; i <= 4; i++) {
            resIds[i * 2] = String.format("plan_%s_%s_%s_%s",
                    parseSpiType(typeString),
                    parsePipeShape(shapeString),
                    String.valueOf(positionIndex),
                    PIPE_DIRECTIONS[i * 2]);
        }
        Log.w(TAG, resIds[2]);
        Log.w(TAG, resIds[8]);
        Log.w(TAG, resIds[4]);
        Log.w(TAG, resIds[6]);

        final String defType = "drawable";
        final String packageName = getContext().getPackageName();

        image_2.setBackgroundResource(resources.getIdentifier(resIds[2], defType, packageName));
        image_4.setBackgroundResource(resources.getIdentifier(resIds[4], defType, packageName));
        image_6.setBackgroundResource(resources.getIdentifier(resIds[6], defType, packageName));
        image_8.setBackgroundResource(resources.getIdentifier(resIds[8], defType, packageName));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (selectIndex == -1) {
                    Toast.makeText(getContext(), "관로의 방향을 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(TAG_DIRECTION, selectIndex, resIds[selectIndex]);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_cancel:
                dismissAllowingStateLoss();
                showPositionDialog();
                break;
            case R.id.btn_close:
                dismissAllowingStateLoss();
                break;
            case R.id.lay_2:
                selectIndex = 2;
                setFocus(v);
                break;
            case R.id.lay_8:
                selectIndex = 8;
                setFocus(v);
                break;
            case R.id.lay_4:
                selectIndex = 4;
                setFocus(v);
                break;
            case R.id.lay_6:
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
        resIds = null;
        super.onDismiss(dialog);
    }

    @Override
    public void onSelect(String tag, int index, String text) {
        if (index == -1) return;
        switch (tag) {
            case TAG_POSITION:
                positionIndex = index;
                break;
            default:
                break;
        }
    }
}
