//package kr.djspi.pipe01.fragment;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.DialogFragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//
//import kr.djspi.pipe01.R;
//
//public class PhotoDialog extends DialogFragment implements OnClickListener {
//
//    private OnSelectListener listener;
//
//    public PhotoDialog() {
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnSelectListener) {
//            listener = (OnSelectListener) context;
//        }
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_photoselect, container, false);
//
//        view.findViewById(R.id.btn_close).setOnClickListener(this);
//        view.findViewById(R.id.lay_camera).setOnClickListener(this);
//        view.findViewById(R.id.lay_gallery).setOnClickListener(this);
//
//        return view;
//    }
//
//    @Override
//    public void onClick(View v) {
//
//    }
//}
