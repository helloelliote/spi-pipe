package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import kr.djspi.pipe01.R;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.BaseActivity.packageName;
import static kr.djspi.pipe01.BaseActivity.resources;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.parsePipeShape;
import static kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.parseSpiType;

public class DirectionDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = DirectionDialog.class.getSimpleName();
    private static final String DEF_TYPE = "drawable";
    private int selectIndex = -1;
    private int positionInt = -1;
    private String dialogTitle;
    private String typeString;
    private String shapeString;
    private String[] resIds;
    private Bundle bundle;
    private ImageView checkView;
    private FragmentManager fragmentManager;
    private OnSelectListener listener;

    public DirectionDialog() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
            positionInt = bundle.getInt("positionInt");
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

        view.findViewById(R.id.button_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        setLayoutVisibility(view);

        return view;
    }

    private void setLayoutVisibility(@NonNull View view) {
        ImageView image_2 = view.findViewById(R.id.image_2);
        ImageView image_8 = view.findViewById(R.id.image_8);
        ImageView image_4 = view.findViewById(R.id.image_4);
        ImageView image_6 = view.findViewById(R.id.image_6);

        resIds = new String[10];
        for (int i = 1; i <= 4; i++) {
            resIds[i * 2] = String.format("plan_%s_%s_%s_%string",
                    parseSpiType(typeString),
                    parsePipeShape(shapeString),
                    String.valueOf(positionInt),
                    PIPE_DIRECTIONS[i * 2]);
        }

        setImageView(image_2, resIds[2]);
        setImageView(image_4, resIds[4]);
        setImageView(image_6, resIds[6]);
        setImageView(image_8, resIds[8]);
    }

    private static void setImageView(ImageView view, String resId) {
        int i = resources.getIdentifier(resId, DEF_TYPE, packageName);
        if (i != 0) view.setBackgroundResource(i);
        else {
            FrameLayout layout = (FrameLayout) view.getParent();
            layout.setVisibility(GONE);
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
                listener.onSelect(TAG_DIRECTION, selectIndex, resIds[selectIndex]);
                if (positionInt == 5) {
                    dismissAllowingStateLoss();
                    return;
                } else {
                    DistanceDialog dialog = new DistanceDialog();
                    bundle.putString("planString", resIds[selectIndex]);
                    dialog.setArguments(bundle);
                    dialog.show(fragmentManager, TAG_DISTANCE);
                    dismissAllowingStateLoss();
                }
                break;
            case R.id.btn_cancel:
                listener.onSelect(TAG_DIRECTION, -2, (String) null);
                dismissAllowingStateLoss();
                break;
            case R.id.button_close:
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

    private void setFocus(@NonNull View view) {
        checkView.setVisibility(INVISIBLE);
        view.findViewById(R.id.v_select).setVisibility(VISIBLE);
        this.checkView = view.findViewById(R.id.v_select);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        selectIndex = -1;
        resIds = null;
        super.onDismiss(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
