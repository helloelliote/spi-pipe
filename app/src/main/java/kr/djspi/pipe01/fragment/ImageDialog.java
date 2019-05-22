package kr.djspi.pipe01.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterInside;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.SpiPhotoObject;

public class ImageDialog extends DialogFragment {

    private RotateTransformation transformation = new RotateTransformation(90f);
    private Uri imageUri;
    private String imageUrl;

    public ImageDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar);
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
                requestBuilder
                        .transform(new CenterInside(), transformation)
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

    /**
     * Must implement equals() and hashCode() for memory caching to work correctly.
     */
    private static class RotateTransformation extends BitmapTransformation {

        private final String ID = getClass().getName();
        private final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));
        private final float rotationAngle;

        RotateTransformation(float rotationAngle) {
            this.rotationAngle = rotationAngle;
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();
            int width = toTransform.getWidth();
            int height = toTransform.getHeight();
            if (width > height) {
                matrix.postRotate(rotationAngle);
            } else {
                matrix.postRotate(0f);
            }
            return Bitmap.createBitmap(toTransform, 0, 0, width, height, matrix, true);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof RotateTransformation;
        }

        @Override
        public int hashCode() {
            return ID.hashCode();
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update(ID_BYTES);
        }
    }
}
