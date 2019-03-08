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
import android.support.media.ExifInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.HashMap;
import java.util.List;

import kr.djspi.pipe01.dto.Pipe_Alt;
import kr.djspi.pipe01.dto.SpiType;

import static android.content.Intent.ACTION_PICK;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.os.Environment.getExternalStorageDirectory;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.support.media.ExifInterface.ORIENTATION_NORMAL;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_180;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_90;
import static android.support.media.ExifInterface.TAG_ORIENTATION;
import static kr.djspi.pipe01.Const.ACTIVITY_REQUEST_CODE_GAL;
import static kr.djspi.pipe01.Const.ACTIVITY_REQUEST_CODE_PHOTO;

public class PipeRecordActivity extends BaseActivity implements OnClickListener, Serializable {

    private static final String TAG = PipeRecordActivity.class.getSimpleName();
    private static FragmentManager fragmentManager;
    private static HashMap<?, ?> hashMap;
    private static List<Pipe_Alt> pipeAltEntries;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static File mPhoto;
    ImageView photoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        hashMap = (HashMap<?, ?>) getIntent().getSerializableExtra("PipeRecordActivity");
        pipeAltEntries = Pipe_Alt.initPipeEntryList();
        setContentView(R.layout.activity_pipe_record);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbarTitle(((SpiType) hashMap.get("spi_type")).getType());
        setPipeCardView();
        // TODO: 2019-03-04 ConfirmButton 기능 추가
    }

    private void setPipeCardView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false));
        PipeCardRecyclerViewAdapter adapter =
                new PipeCardRecyclerViewAdapter(pipeAltEntries);
        recyclerView.addItemDecoration(new PipeGridItemDecoration(20, 20));
        recyclerView.setAdapter(adapter);
    }

    @Override
    boolean useToolbar() {
        return true;
    }

    @Override
    protected void setToolbarTitle(String title) {
        if (title != null) toolbar.setTitle(String.format("SPI 매설관로 %s", title));
    }

    @Override
    public void onClick(View v) {

    }

    private class OnPhotoInput {

        private static final int MAX_PHOTO_SIZE = 1024;
        private String filePathAbs;
        private Uri fileUri;

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
