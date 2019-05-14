package kr.djspi.pipe01.fragment;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.RequestBuilder;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.SpiPhotoObject;

public class ImageDialog extends DialogFragment {

    private Uri imageUri;
    private String imageUrl;

    public ImageDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            SpiPhotoObject photoObj = (SpiPhotoObject) bundle.getSerializable("SpiPhotoObject");
            if (photoObj != null) {
                imageUri = photoObj.getUri();
                imageUrl = photoObj.getUrl();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);

        ImageView imageView = view.findViewById(R.id.image_view);
        RequestBuilder<Drawable> requestBuilder = null;
        try {
            if (imageUri != null) {
                requestBuilder = Glide.with(view).load(imageUri);
            } else if (imageUrl != null) {
                requestBuilder = Glide.with(view).load(imageUrl);
            }
            if (requestBuilder != null) {
                requestBuilder.fitCenter()
                        .dontAnimate()
                        .into(imageView)
                        .clearOnDetach();
            }
        } catch (NullPointerException ignore) {
        }
        imageView.setOnClickListener(v -> ImageDialog.this.dismissAllowingStateLoss());
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        imageUri = null;
        imageUrl = null;
    }
}
