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
import android.support.design.button.MaterialButton;
import android.support.media.ExifInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.PIPE_SHAPES;
import static kr.djspi.pipe01.Const.REQUEST_CODE_GALLERY;
import static kr.djspi.pipe01.Const.REQUEST_CODE_MAP;
import static kr.djspi.pipe01.Const.REQUEST_CODE_PHOTO;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.TAG_TYPE_COLUMN;
import static kr.djspi.pipe01.Const.TAG_TYPE_MARKER;
import static kr.djspi.pipe01.Const.TAG_TYPE_PLATE;
import static kr.djspi.pipe01.Const.URL_TEST;

public class RecordInputActivity extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    private static final String TAG = RecordInputActivity.class.getSimpleName();
    private static ExtendedEditText ePipe, eShape, ePosition, eHorizontal, eVertical, eDepth, eSpec, eMaterial,
            eSupervise, eSuperviseContact, eSpiMemo, eConstruction, eConstructionContact, photo, gallery;
    private static HashMap<?, ?> itemMap;
    private static String spiType, header, unit;
    private static Pipe pipe = new Pipe();
    private static PipeType pipeType = new PipeType();
    private static PipeShape pipeShape = new PipeShape();
    private static PipePosition pipePosition = new PipePosition();
    private static PipeSupervise pipeSupervise = new PipeSupervise();
    public static final PipeTypeEnum[] pipes = PipeTypeEnum.values();
    public static FragmentManager fragmentManager;
    public static ArrayList<String> superviseList;
    private MaterialButton buttonConfirm;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static TextFieldBoxes tPipe, tShape, tPosition, tHorizontal, tVertical, tDepth,
            tSpec, tMaterial, tSupervise, tSuperviseContact;
    static int requestCode;
    static OnPhotoInput onPhotoInput;
    static File mPhoto;
    static SpiLocation spiLocation = new SpiLocation();
    ImageView photoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        Serializable serializable = getIntent().getSerializableExtra("PipeRecordActivity");
        if (serializable instanceof HashMap) itemMap = (HashMap) serializable;
        spiType = ((SpiType) Objects.requireNonNull(itemMap.get("spi_type"))).getType();
//        onPhotoInput = new OnPhotoInput();
        setContentView(R.layout.activity_record_input);
        superviseList = getSuperviseList();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbarTitle(spiType);

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
        buttonConfirm = findViewById(R.id.btn_confirm);
        buttonConfirm.setOnClickListener(new OnNextButtonClick());

        eHorizontal.setText("2.45");
        eVertical.setText("1.10");
        eDepth.setText("4.50");
        eSpec.setText("250");
        eMaterial.setText("알루미늄");
        eSuperviseContact.setText("053-424-9547");
        eSpiMemo.setText("테스트 메모");
//        eConstruction.setText("서울시상수도사업본부");
//        eConstructionContact.setText("02-493-3904");
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

    private ArrayList<String> getSuperviseList() {
        if (superviseList == null) {
            superviseList = new ArrayList<>();
            JsonObject jsonQuery = new JsonObject();
            jsonQuery.addProperty("json", "");
            Retrofit2x.builder()
                    .setService(new SuperviseGet(URL_TEST))
                    .setQuery(jsonQuery)
                    .build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            final JsonArray jsonArray = response.get("data").getAsJsonArray();
                            for (JsonElement element : jsonArray) {
                                superviseList.add(element.getAsJsonObject().get("supervise").getAsString());
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Toast.makeText(context, "관리기관: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return superviseList;
        } else return superviseList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.l_pipe:
                ListDialog.get().show(fragmentManager, TAG_PIPE);
                break;
            case R.id.l_shape:
                pipeShape.setShape(null);
                ListDialog.get().show(fragmentManager, TAG_SHAPE);
                break;
            case R.id.l_supervise:
                if (superviseList.isEmpty()) {
                    tSupervise.setOnClickListener(null);
                    eSupervise.setEnabled(true);
                    eSupervise.setHint("직접 입력해주세요.");
                    return;
                }
                ListDialog.get().show(fragmentManager, TAG_SUPERVISE);
                break;
            case R.id.l_position:
                if (pipeShape.getShape() == null) {
                    ePosition.setHint("관로형태를 먼저 선택해 주세요.");
                    tPosition.setEndIcon(R.drawable.ic_shape);
                    tPosition.getEndIconImageButton()
                            .setOnClickListener(v1 -> ListDialog.get().show(fragmentManager, TAG_SHAPE));
                    return;
                }
                PositionDialog.get().show(fragmentManager, spiType);
                break;
//            case R.id.l_photo:
//                requestCode = REQUEST_CODE_PHOTO;
//                onPhotoInput.setPhoto();
//                break;
//            case R.id.l_gallery:
//                requestCode = REQUEST_CODE_GALLERY;
//                onPhotoInput.setGallery();
//                break;
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
                pipeType.setId(index + 1);
                pipe.setType_id(index + 1);
                header = pipes[index].getHeader();
                eSpec.setPrefix(header + "  ");
                unit = pipes[index].getUnit();
                eSpec.setSuffix("  " + unit);
                eSpec.setInputType(index == 5 ? TYPE_CLASS_TEXT : TYPE_CLASS_NUMBER); // index == 5 : 통신관로
                break;
            case TAG_SHAPE:
                eShape.setText(PIPE_SHAPES[index]);
                ePosition.setHint(R.string.popup_hint);
                tPosition.setEndIcon(null);
                break;
            case TAG_SUPERVISE:
                eSupervise.setText(superviseList.get(index));
                pipeSupervise.setId(index + 1);
                pipe.setSupervise_id(index + 1);
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
                        eHorizontal.setEnabled(false);
                        eHorizontal.setPrefix("없음");
                        eHorizontal.setText("0");
                        eVertical.setEnabled(true);
                        ePosition.setText("차도 / 길어깨쪽 방향");
                        break;
                    case 3:
                        eHorizontal.setEnabled(true);
                        eHorizontal.setPrefix("우측");
                        eVertical.setEnabled(true);
                        ePosition.setText("차도 / 길어깨쪽 방향");
                        break;
                    case 4:
                        eHorizontal.setEnabled(true);
                        eHorizontal.setPrefix("좌측");
                        eVertical.setEnabled(false);
                        eVertical.setPrefix("없음");
                        eVertical.setText("0");
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 5:
                        eHorizontal.setEnabled(false);
                        eHorizontal.setPrefix("없음");
                        eHorizontal.setText("0");
                        eVertical.setEnabled(false);
                        eVertical.setPrefix("없음");
                        eVertical.setText("0");
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 6:
                        eHorizontal.setEnabled(true);
                        eHorizontal.setPrefix("우측");
                        eVertical.setEnabled(false);
                        eVertical.setPrefix("없음");
                        eVertical.setText("0");
                        ePosition.setText("보차도 경계 위치");
                        break;
                    case 7:
                        eHorizontal.setEnabled(true);
                        eHorizontal.setPrefix("좌측");
                        eVertical.setEnabled(true);
                        ePosition.setText("보도 / 차도 반대쪽 방향");
                        break;
                    case 8:
                        eHorizontal.setEnabled(false);
                        eHorizontal.setPrefix("없음");
                        eHorizontal.setText("0");
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
                        eHorizontal.setPrefix("수평");
                        eVertical.setEnabled(true);
                        ePosition.setText(null);
                        break;
                }
                break;
            case TAG_DIRECTION:
                pipePosition.setDirection(PIPE_DIRECTIONS[index]);
                break;
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
                case REQUEST_CODE_PHOTO:
//                    onPhotoInput.getPhoto();
                    break;
                case REQUEST_CODE_GALLERY:
//                    onPhotoInput.getGallery(data.getData());
                    break;
                case REQUEST_CODE_MAP:
                    double[] spiLocationArray = data.getDoubleArrayExtra("SpiLocation");
                    spiLocation.setLatitude(spiLocationArray[0]);
                    spiLocation.setLongitude(spiLocationArray[1]);
                    spiLocation.setCount(0);
                    buttonConfirm.setText(getString(R.string.record_confirm));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        pipeShape.setShape(null);
        super.onResume();
    }

    @Override
    public void onPause() {
        if (spiLocation != null) spiLocation.setCount(-1);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        return;
    }

    @NotNull
    @Contract(" -> new")
    private static Entry setEntry() throws Exception {
        Spi spi = (Spi) itemMap.get("spi");
        final int spiId = Objects.requireNonNull(spi).getId();
        SpiType spiType = (SpiType) itemMap.get("spi_type");
        SpiMemo spiMemo = new SpiMemo(eSpiMemo.getText().toString());
        spiLocation.setSpi_id(spiId);
        pipe.setSpi_id(spiId);
        pipe.setDepth(Double.valueOf(eDepth.getText().toString()));
        pipe.setMaterial(eMaterial.getText().toString());
        pipeShape.setSpec(eSpec.getText().toString());
        pipeType.setHeader(header);
        pipeType.setPipe(ePipe.getText().toString());
        pipeType.setUnit(unit);
        pipePosition.setHorizontal(Double.valueOf(eHorizontal.getText().toString()));
        pipePosition.setVertical(Double.valueOf(eVertical.getText().toString()));
        pipeSupervise.setSupervise(eSupervise.getText().toString());
        pipeSupervise.setContact(eSuperviseContact.getText().toString());
        pipe.setConstruction(eConstruction.getText().toString());
        pipe.setConstruction_contact(eConstructionContact.getText().toString());

        return new Entry(
                spi, spiType, spiLocation, spiMemo,
                pipe, pipeType, pipeShape, pipePosition, pipeSupervise);
    }

    @SuppressWarnings("ALL")
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
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
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
            startActivityForResult(intent, REQUEST_CODE_GALLERY);
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
            if (spiLocation.getCount() != 1) { // 아직 위치 정보가 기록되지 않음
                startActivityForResult(new Intent(context, SpiLocationActivity.class), REQUEST_CODE_MAP);
            } else try {
                final Entry entry = setEntry();
                ArrayList<Entry> entries = new ArrayList<>(1);
                entries.add(entry);
                startActivity(new Intent(context, RecordWriteActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("entry", entries));
            } catch (Exception e) {
                showMessagePopup(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.");
                e.printStackTrace();
            }
        }

//        private boolean isAllValid() {
//            boolean isValid = false;
//
//            final TextFieldBoxes[] fields =
//                    {tPipe, tShape, tHorizontal, tVertical, tDepth,
//                            tSpec, tMaterial, tSupervise, tSuperviseContact};
//
//            for (TextFieldBoxes field : fields) {
//                if (!field.validate()) {
//
//                }
//            }
//
//            return isValid;
//        }
    }
}
