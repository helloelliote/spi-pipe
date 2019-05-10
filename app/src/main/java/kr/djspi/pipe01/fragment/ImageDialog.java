package kr.djspi.pipe01.fragment;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.SpiPhotoObject;

public class ImageDialog extends DialogFragment {

    private Uri imageUri;

    public ImageDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            imageUri = ((SpiPhotoObject) bundle.getSerializable("SpiPhotoObject")).getUri();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);

        ImageView imageView = view.findViewById(R.id.image_view);
        Glide.with(view).load(imageUri)
                .fitCenter()
                .dontAnimate()
                .into(imageView)
                .clearOnDetach();
        imageView.setOnClickListener(v -> dismissAllowingStateLoss());

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        imageUri = null;
    }
}
