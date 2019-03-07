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
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.djspi.pipe01.dto.Pipe;
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SuperviseGet;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

import static android.content.Intent.ACTION_PICK;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.os.Environment.getExternalStorageDirectory;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.support.media.ExifInterface.ORIENTATION_NORMAL;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_180;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_90;
import static android.support.media.ExifInterface.TAG_ORIENTATION;
import static kr.djspi.pipe01.NaverMapActivity.URL_SPI;

public class RecordInputActivity extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    private static final String TAG = RecordInputActivity.class.getSimpleName();
    public static final String TAG_PIPE = "pipe";
    public static final String TAG_SHAPE = "shape";
    public static final String TAG_SUPER = "supervise";
    private static FragmentManager fragmentManager;
    private static HashMap<?, ?> hashMap;
    private static List<Pipe> pipeEntries;
    public static ArrayList<String> listSupervise;
    public static final PipeTypeEnum[] pipes = PipeTypeEnum.values();
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    TextFieldBoxes l_pipe, l_shape, l_horizontal, l_vertical, l_depth,
            l_spec, l_material, l_supervise, l_supervise_contact;
    ExtendedEditText pipe, shape, horizontal, vertical, depth, spec, material,
            supervise, supervise_contact, spi_memo, construction, construction_contact, photo, gallery;
    ImageView photoView;
    static final int ACTIVITY_REQUEST_CODE_PHOTO = 10001;
    static final int ACTIVITY_REQUEST_CODE_GAL = 10002;
    static int requestCode;
    static OnPhotoInput onPhotoInput;
    static File mPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        hashMap = (HashMap<?, ?>) getIntent().getSerializableExtra("PipeRecordActivity");
        pipeEntries = Pipe.initPipeEntryList();
        listSupervise = getListSupervise();
//        onPhotoInput = new OnPhotoInput();
        setContentView(R.layout.activity_record_input);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbarTitle(((SpiType) hashMap.get("spi_type")).getType());
        // TODO: 2019-03-04 ConfirmButton 기능 추가
        l_pipe = findViewById(R.id.l_pipe);
        l_pipe.setOnClickListener(this);
        pipe = findViewById(R.id.pipe);
        pipe.setEnabled(false);

        l_shape = findViewById(R.id.l_shape);
        l_shape.setOnClickListener(this);
        shape = findViewById(R.id.shape);
        shape.setEnabled(false);

        horizontal = findViewById(R.id.horizontal);
        vertical = findViewById(R.id.vertical);
        depth = findViewById(R.id.depth);
        spec = findViewById(R.id.spec);
        material = findViewById(R.id.material);

        l_supervise = findViewById(R.id.l_supervise);
        l_supervise.setOnClickListener(this);
        supervise = findViewById(R.id.supervise);
        supervise.setEnabled(false);

        supervise_contact = findViewById(R.id.supervise_contact);
        supervise_contact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        spi_memo = findViewById(R.id.spi_memo);
        construction = findViewById(R.id.construction);
        construction_contact = findViewById(R.id.construction_contact);
        supervise_contact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // TODO: 2019-03-07 사진 추가 기능 개발
//        l_photo = findViewById(R.id.l_photo);
//        l_photo.setOnClickListener(this);
//        l_gallery = findViewById(R.id.l_gallery);
//        l_gallery.setOnClickListener(this);

        findViewById(R.id.btn_cancel).setOnClickListener(v -> RecordInputActivity.this.onBackPressed());
        findViewById(R.id.btn_confirm).setOnClickListener(new OnNextButtonClick());
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
        if (ListDialog.get().isAdded()) return;
        switch (v.getId()) {
            case R.id.l_pipe:
                ListDialog.get().show(fragmentManager, TAG_PIPE);
                break;
            case R.id.l_shape:
                ListDialog.get().show(fragmentManager, TAG_SHAPE);
                break;
            case R.id.l_supervise:
                ListDialog.get().show(fragmentManager, TAG_SUPER);
                break;
//            case R.id.l_photo:
//                requestCode = ACTIVITY_REQUEST_CODE_PHOTO;
//                onPhotoInput.setPhoto();
//                break;
//            case R.id.l_gallery:
//                requestCode = ACTIVITY_REQUEST_CODE_GAL;
//                onPhotoInput.setGallery();
//                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (mPhoto != null && mPhoto.isFile()) {
                mPhoto.delete();
                mPhoto = null;
            }
            switch (requestCode) {
                case ACTIVITY_REQUEST_CODE_PHOTO:
//                    onPhotoInput.getPhoto();
                    break;
                case ACTIVITY_REQUEST_CODE_GAL:
//                    onPhotoInput.getGallery(data.getData());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onSelect(String tag, int index) {
        if (index == -1) return;
        switch (tag) {
            case TAG_PIPE:
                pipe.setText(getString(pipes[index].getNameRes()));
                spec.setPrefix(pipes[index].getHeader());
                spec.setSuffix(pipes[index].getUnit());
                spec.setInputType(index == 5 ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                break;
            case TAG_SHAPE:
                shape.setText(resources.getStringArray(R.array.popup_list_shape)[index]);
                break;
            case TAG_SUPER:
                supervise.setText(listSupervise.get(index));
            default:
                break;
        }
    }

    private ArrayList<String> getListSupervise() {
        if (listSupervise == null) {
            listSupervise = new ArrayList<>();
            JsonObject jsonQuery = new JsonObject();
            jsonQuery.addProperty("json", "");
            Retrofit2x.builder()
                    .setService(new SuperviseGet(URL_SPI))
                    .setQuery(jsonQuery)
                    .build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            JsonArray jsonArray = response.get("data").getAsJsonArray();
                            for (JsonElement obj : jsonArray) {
                                listSupervise.add(obj.getAsString());
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            showMessagePopup(6, throwable.getMessage());
                        }
                    });
            return listSupervise;
        } else return listSupervise;
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
            File dir = new File(getExternalStorageDirectory() + "/SPI/");
            if (!dir.exists()) dir.mkdirs();
            String fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File filePath = new File(
                    getExternalStorageDirectory().getAbsoluteFile()
                            + "/SPI/"
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
            // l_horizontal, l_vertical, l_depth,
            //            l_spec, l_material, l_supervise, l_supervise_contact
            boolean isAllValid = false;
            InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            l_pipe.setMinCharacters(1);
            if (!l_pipe.validate()){
                l_pipe.setCounterTextColor(android.R.color.white);
                l_pipe.setError("필수입력 항목입니다.", true);
            }

            if (isAllValid) {

            }
        }
    }
}
