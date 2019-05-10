package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import kr.djspi.pipe01.R;

import static kr.djspi.pipe01.Const.TAG_PHOTO;

public class PhotoDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = PhotoDialog.class.getSimpleName();
    private int selectIndex = -1;
    private String dialogTitle;
    private OnSelectListener listener;

    public PhotoDialog() {
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
        dialogTitle = getString(R.string.popup_title_select_photo);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);

        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_camera).setOnClickListener(this);
        view.findViewById(R.id.btn_gallery).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                selectIndex = 1;
                listener.onSelect(TAG_PHOTO, selectIndex, (String) null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_gallery:
                selectIndex = 2;
                listener.onSelect(TAG_PHOTO, selectIndex, (String) null);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_close:
                dismissAllowingStateLoss();
            default:
                break;
        }
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
