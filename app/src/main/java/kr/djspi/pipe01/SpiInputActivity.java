package kr.djspi.pipe01;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.media.ExifInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.ACTION_PICK;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.os.Environment.getExternalStorageDirectory;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.support.media.ExifInterface.ORIENTATION_NORMAL;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_180;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_90;
import static android.support.media.ExifInterface.TAG_ORIENTATION;

public class SpiInputActivity extends BaseActivity implements OnClickListener, Serializable {

    private static final String TAG = SpiInputActivity.class.getSimpleName();
    public static FragmentManager fragmentManager;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static File mPhoto;
    ImageView photoView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spi_input);
        fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        setToolbarTitle(null);
        setInput();
    }

    @Override
    boolean useToolbar() {
        return true;
    }

    @Override
    protected void setToolbarTitle(String string) {
        super.setToolbarTitle(string);
        if (string != null) toolbar.setTitle(string);
    }

    public void setInput() {
        TextInputLayout inputLayout = findViewById(R.id.inputLayout1);
        TextInputEditText editText = findViewById(R.id.editText1);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > inputLayout.getCounterMaxLength()) {
                    inputLayout.setError("글자수를 초과하였습니다");
                } else {
                    inputLayout.setError(null);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {

    }

    private void saveState() {

    }

    private void restoreState() {

    }

    private class OnPhotoInput {

        private static final int MAX_PHOTO_SIZE = 1024;
        private String filePathAbs;
        private Uri fileUri;
        static final int ACTIVITY_REQUEST_CODE_PHOTO = 10001;
        static final int ACTIVITY_REQUEST_CODE_GAL = 10002;

        private void setPhoto() {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File filePath = getFilePath();
                    if (filePath != null) {
                        fileUri = FileProvider.getUriForFile(context, getPackageName(), filePath);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(intent, ACTIVITY_REQUEST_CODE_PHOTO);
                    }
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        private File getFilePath() {
            File dir = new File(getExternalStorageDirectory() + "/path/");
            if (!dir.exists()) dir.mkdirs();
            String fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File filePath = new File(
                    getExternalStorageDirectory().getAbsoluteFile()
                            + "/path/"
                            + fileDate
                            + ".jpg");
            filePathAbs = filePath.getAbsolutePath();
            return filePath;
        }

        private void setGallery() {
            Intent intent = new Intent(ACTION_PICK);
            intent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, ACTIVITY_REQUEST_CODE_GAL);
        }

        private void getPhoto() {
            try {
                Bitmap sourcePhoto = BitmapFactory.decodeFile(filePathAbs);
                Bitmap editPhoto = getEditPhoto(sourcePhoto, filePathAbs);
                OutputStream outputStream = null;
                photoView.setImageBitmap(editPhoto);
//                mTxtInputPhoto.setText("");
                mPhoto = new File(context.getFilesDir().getAbsolutePath() + "temp.jpg");
                if (mPhoto.createNewFile()) {
                    outputStream = new FileOutputStream(mPhoto);
                    editPhoto.compress(JPEG, 100, outputStream);
                }
                if (outputStream != null) outputStream.close();
                if (mPhoto.isFile()) {
//                SPIDataItem item = new SPIDataItem();
//                item.setVal(mPhoto.getPath());
//                spiData.setItem(KEY_PHOTO.getKey(), item);
//                mTxtPhoto.setText(mPhoto.getName());
                }
                sourcePhoto.recycle();
                editPhoto.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Bitmap getEditPhoto(Bitmap source, String path) throws IOException {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL);
            int exifDegree;
            switch (exifOrientation) {
                case ORIENTATION_ROTATE_90:
                    exifDegree = 90;
                    break;
                case ORIENTATION_ROTATE_180:
                    exifDegree = 180;
                    break;
                case ORIENTATION_ROTATE_270:
                    exifDegree = 270;
                    break;
                default:
                    exifDegree = 0;
                    break;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(exifDegree);

            int width = source.getWidth();
            int height = source.getHeight();

            Bitmap rotatePhoto = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);

            final float scaleRatio = (float) width / (float) height;
            if (scaleRatio > 1) {
                width = MAX_PHOTO_SIZE;
                height = (int) (width / scaleRatio);
            } else {
                height = MAX_PHOTO_SIZE;
                width = (int) (height * scaleRatio);
            }

            Bitmap scalePhoto = Bitmap.createScaledBitmap(rotatePhoto, width, height, true);
            rotatePhoto.recycle();
            return scalePhoto;
        }

        private void deletePhoto() {
            // TODO: 2018-11-19 mTxtInputPhoto 다시 힌트메시지와 함께 나타나게 하기
//            SPIDataItem item = new SPIDataItem();
//            item.setVal("");
//            spiData.setItem(KEY_PHOTO.getKey(), item);
//            mTxtPhoto.setText("");
//            mLayPhoto.setVisibility(GONE);
//            onPhotoInput = null;
        }

        private void showPhoto() {
            // TODO: 이미지 선택시 전체화면으로 보여주기 구현
//        mParentView.showPopup(new p_Common_Photo(mActivity, mParentView), mPhoto, new onPopupActionListener() {
//            @Override
//            public void onPopupAction(int action, Object ret) {
//            }
//        }, null);
        }
    }

    private class OnNextButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            boolean isAllValid = false;
            InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (isAllValid) {

            }
        }
    }
}
