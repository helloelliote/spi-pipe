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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
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
import java.util.Objects;

import kr.djspi.pipe01.dto.Entry;
import kr.djspi.pipe01.dto.Pipe;
import kr.djspi.pipe01.dto.PipePosition;
import kr.djspi.pipe01.dto.PipeShape;
import kr.djspi.pipe01.dto.PipeSupervise;
import kr.djspi.pipe01.dto.PipeType;
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMemo;
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.PlotDialog;
import kr.djspi.pipe01.fragment.PositionDialog;
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
import static kr.djspi.pipe01.Const.ACTIVITY_REQUEST_CODE_GAL;
import static kr.djspi.pipe01.Const.ACTIVITY_REQUEST_CODE_PHOTO;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;
import static kr.djspi.pipe01.Const.URL_SPI;

public class RecordInputActivity extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    private static final String TAG = RecordInputActivity.class.getSimpleName();
    private static HashMap<?, ?> itemMap;
    public static final PipeTypeEnum[] pipes = PipeTypeEnum.values();
    public static FragmentManager fragmentManager;
    public static ArrayList<String> listSupervise;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static TextFieldBoxes tPipe, tShape, tPosition, tHorizontal, tVertical, tDepth,
            tSpec, tMaterial, tSupervise, tSuperviseContact;
    static ExtendedEditText ePipe, eShape, ePosition, eHorizontal, eVertical, eDepth, eSpec, eMaterial,
            eSupervise, eSuperviseContact, eSpiMemo, eConstruction, eConstructionContact, photo, gallery;
    ImageView photoView;
    static String spiType, header, unit;
    static int requestCode;
    static OnPhotoInput onPhotoInput;
    static File mPhoto;
    public static Pipe pipe = new Pipe();
    public static PipeType pipeType = new PipeType();
    public static PipeShape pipeShape = new PipeShape();
    public static PipePosition pipePosition = new PipePosition();
    public static PipeSupervise pipeSupervise = new PipeSupervise();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        itemMap = (HashMap<?, ?>) getIntent().getSerializableExtra("PipeRecordActivity");
        listSupervise = getListSupervise();
        spiType = ((SpiType) itemMap.get("spi_type")).getType();
//        onPhotoInput = new OnPhotoInput();
        setContentView(R.layout.activity_record_input);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbarTitle(spiType);
        // TODO: 2019-03-04 ConfirmButton 기능 추가
        tPipe = findViewById(R.id.l_pipe);
        tPipe.setOnClickListener(this);
        ePipe = findViewById(R.id.pipe);
        ePipe.setEnabled(false);

        tShape = findViewById(R.id.l_shape);
        tShape.setOnClickListener(this);
        eShape = findViewById(R.id.shape);
        eShape.setEnabled(false);
        tShape.setSimpleTextChangeWatcher((theNewText, isError) ->
                pipeShape.setShape(theNewText));

        tPosition = findViewById(R.id.l_position);
        tPosition.setOnClickListener(this);
        ePosition = findViewById(R.id.position);
        ePosition.setEnabled(false);

        tHorizontal = findViewById(R.id.l_horizontal);
        eHorizontal = findViewById(R.id.horizontal);

        tVertical = findViewById(R.id.l_vertical);
        eVertical = findViewById(R.id.vertical);

        tDepth = findViewById(R.id.l_depth);
        eDepth = findViewById(R.id.depth);

        tSpec = findViewById(R.id.l_spec);
        eSpec = findViewById(R.id.spec);

        tMaterial = findViewById(R.id.l_material);
        eMaterial = findViewById(R.id.material);

        tSupervise = findViewById(R.id.l_supervise);
        tSupervise.setOnClickListener(this);
        eSupervise = findViewById(R.id.supervise);
        eSupervise.setEnabled(false);

        tSuperviseContact = findViewById(R.id.l_supervise_contact);
        eSuperviseContact = findViewById(R.id.supervise_contact);
        eSuperviseContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        eSpiMemo = findViewById(R.id.spi_memo);
        eConstruction = findViewById(R.id.construction);
        eConstructionContact = findViewById(R.id.construction_contact);
        eConstructionContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // TODO: 2019-03-07 사진 추가 기능 개발
//        l_photo = findViewById(R.id.l_photo);
//        l_photo.setOnClickListener(this);
//        l_gallery = findViewById(R.id.l_gallery);
//        l_gallery.setOnClickListener(this);

        findViewById(R.id.btn_cancel).setOnClickListener(v -> RecordInputActivity.this.onBackPressed());
        findViewById(R.id.btn_confirm).setOnClickListener(new OnNextButtonClick());

        ePipe.setText("상수관로");
        eHorizontal.setText("2.45");
        eVertical.setText("1.10");
        eDepth.setText("4.50");
        eSpec.setText("250");
        eMaterial.setText("알루미늄");
//        eSupervise.setText("");
        eSuperviseContact.setText("053-424-9547");
        eSpiMemo.setText("테스트 메모");
        eConstruction.setText("서울시상수도사업본부");
        eConstructionContact.setText("02-493-3904");
    }

    @Override
    boolean useToolbar() {
        return true;
    }

    @Override
    protected void setToolbarTitle(String title) {
        if (title != null) {
            toolbar.setTitle(String.format(getString(R.string.app_title_alt), "매설관로", title));
        }
    }

    @Override
    public void onClick(View v) {
        if (ListDialog.get().isAdded()) return;
        switch (v.getId()) {
            case R.id.l_pipe:
                ListDialog.get().show(fragmentManager, TAG_PIPE);
                break;
            case R.id.l_shape:
                pipeShape.setShape(null);
                ListDialog.get().show(fragmentManager, TAG_SHAPE);
                break;
            case R.id.l_supervise:
                ListDialog.get().show(fragmentManager, TAG_SUPERVISE);
                break;
            case R.id.l_position:
                if (pipeShape.getShape() == null) {
                    ePosition.setHint("관로형태를 먼저 선택해 주세요.");
                    tPosition.setEndIcon(R.drawable.ic_shape);
                    tPosition.getEndIconImageButton().setOnClickListener(v1 ->
                            ListDialog.get().show(fragmentManager, TAG_SHAPE));
                    return;
                } else {
                    PlotDialog plotDialog = PositionDialog.get();
                    Bundle bundle = new Bundle(1);
//                bundle.putSerializable();
                    plotDialog.show(fragmentManager, spiType);
                    break;
//            case R.id.l_photo:
//                requestCode = ACTIVITY_REQUEST_CODE_PHOTO;
//                onPhotoInput.setPhoto();
//                break;
//            case R.id.l_gallery:
//                requestCode = ACTIVITY_REQUEST_CODE_GAL;
//                onPhotoInput.setGallery();
//                break;
                }
            case R.id.btn_confirm:
                Toast.makeText(context, "K", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelect(String tag, int index) {
        if (index == -1) return;
        switch (tag) {
            case TAG_PIPE:
                ePipe.setText(getString(pipes[index].getNameRes()));
                pipe.setType_id(index);
                pipeType.setId(index);
                pipePosition.setPipe_id(index);
                pipeShape.setPipe_id(index);
                // TODO: 2019-03-09 서버에서의 관로타입 index 는 몇번부터 시작하는지 확인
                header = pipes[index].getHeader();
                eSpec.setPrefix(header + "  ");
                unit = pipes[index].getUnit();
                eSpec.setSuffix("  " + unit);
                eSpec.setInputType(index == 5 ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                break;
            case TAG_SHAPE:
                eShape.setText(resources.getStringArray(R.array.popup_list_shape)[index]);
                ePosition.setHint(R.string.popup_hint);
                tPosition.setEndIcon(null);
                break;
            case TAG_SUPERVISE:
                eSupervise.setText(listSupervise.get(index));
                pipe.setSupervise_id(index);
                pipeSupervise.setId(index);
                // TODO: 2019-03-09 서버에서의 관리기관 index 는 몇번부터 시작하는지 확인
                break;
            case TAG_TYPE_PLATE:
                pipePosition.setPosition(index);
                break;
            case TAG_TYPE_MARKER:
                pipePosition.setPosition(index);
                break;
            case TAG_TYPE_COLUMN:
                pipePosition.setPosition(index);
                break;
            case TAG_POSITION:
                pipePosition.setPosition(index);
                switch (index) {
                    case 1:
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(true);
                        eHorizontal.setPrefix("좌측");
                        ePosition.setText("차도 / 길어깨쪽 방향");
                        break;
                    case 2:
                        eHorizontal.setText("0");
                        eHorizontal.setPrefix("없음");
                        eHorizontal.setEnabled(false);
                        eVertical.setEnabled(true);
                        ePosition.setText("차도 / 길어깨쪽 방향");
                        break;
                    case 3:
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(true);
                        eHorizontal.setPrefix("우측");
                        ePosition.setText("차도 / 길어깨쪽 방향");
                        break;
                    case 4:
                        eVertical.setText("0");
                        eVertical.setPrefix("없음");
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(false);
                        eHorizontal.setPrefix("좌측");
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 5:
                        eHorizontal.setText("0");
                        eVertical.setText("0");
                        eHorizontal.setPrefix("없음");
                        eVertical.setPrefix("없음");
                        eHorizontal.setEnabled(false);
                        eVertical.setEnabled(false);
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 6:
                        eVertical.setText("0");
                        eVertical.setPrefix("없음");
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(false);
                        eHorizontal.setPrefix("우측");
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 7:
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(true);
                        eHorizontal.setPrefix("좌측");
                        ePosition.setText("보도 / 차도 반대쪽 방향");
                        break;
                    case 8:
                        eHorizontal.setText("0");
                        eHorizontal.setPrefix("없음");
                        eHorizontal.setEnabled(false);
                        eVertical.setEnabled(true);
                        ePosition.setText("보도 / 차도 반대쪽 방향");
                        break;
                    case 9:
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(true);
                        eHorizontal.setPrefix("우측");
                        ePosition.setText("보도 / 차도 반대쪽 방향");
                        break;
                    default:
                        eHorizontal.setEnabled(true);
                        eVertical.setEnabled(true);
                        eHorizontal.setPrefix("수평");
                        ePosition.setText(null);
                        break;
                }
                // TODO: 2019-03-08 수평, 수직을 좌,우로 바꾸기
                break;
            case TAG_DIRECTION:
                pipePosition.setDirection(PIPE_DIRECTIONS[index]);
                break;
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

    private static Entry setEntry() throws Exception {
        Spi spi = (Spi) itemMap.get("spi");
        final int spiId = Objects.requireNonNull(spi).getId();
        SpiType spiType = (SpiType) itemMap.get("spi_type");
        // TODO: 2019-03-08 SpiLocation
        SpiLocation spiLocation = new SpiLocation(spiId, spiId);
        SpiMemo spiMemo = new SpiMemo(eSpiMemo.getText().toString());
        pipe.setSpi_id(spiId);
        pipe.setDepth(Double.valueOf(eDepth.getText().toString()));
        pipe.setSpec(Integer.valueOf(eSpec.getText().toString()));
        pipe.setMaterial(eMaterial.getText().toString());
        pipeType.setHeader(header);
        pipeType.setPipe(ePipe.getText().toString());
        pipeType.setUnit(unit);
        pipePosition.setHorizontal(Double.valueOf(eHorizontal.getText().toString()));
        pipePosition.setVertical(Double.valueOf(eVertical.getText().toString()));
        pipeSupervise.setSupervise(eSupervise.getText().toString());
        pipeSupervise.setContact(eSuperviseContact.getText().toString());
        pipe.setConstruction(eConstruction.getText().toString());
        pipe.setConstructionContact(eConstructionContact.getText().toString());

        return new Entry(
                spi, spiType, spiLocation, spiMemo,
                pipe, pipeType, pipeShape, pipePosition, pipeSupervise);
    }

    private class OnNextButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                final Entry entry = setEntry();
                ArrayList<Entry> entries = new ArrayList<>(2);
                entries.add(entry);
                Log.w(TAG, new Gson().toJson(entries));
            } catch (Exception e) {
                showMessagePopup(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.");
            }
        }

        private boolean isAllValid() {
            boolean isValid = false;

            final TextFieldBoxes[] fields =
                    {tPipe, tShape, tHorizontal, tVertical, tDepth,
                            tSpec, tMaterial, tSupervise, tSuperviseContact};

            for (TextFieldBoxes field : fields) {
                if (!field.validate()) {

                }
            }

            return isValid;
        }
    }

    @Override
    public void onResume() {
        pipeShape.setShape(null);
        super.onResume();
    }
}
