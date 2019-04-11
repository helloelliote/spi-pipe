package kr.djspi.pipe01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.filter.DecimalFilter;
import com.helloelliote.util.image.ImageUtil;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SuperviseGet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import kr.djspi.pipe01.dto.Entry;
import kr.djspi.pipe01.dto.Pipe;
import kr.djspi.pipe01.dto.PipePlan;
import kr.djspi.pipe01.dto.PipePosition;
import kr.djspi.pipe01.dto.PipeShape;
import kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum;
import kr.djspi.pipe01.dto.PipeSupervise;
import kr.djspi.pipe01.dto.PipeType;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMemo;
import kr.djspi.pipe01.dto.SpiPhoto;
import kr.djspi.pipe01.dto.SpiPhotoObject;
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.fragment.ImageDialog;
import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.PhotoDialog;
import kr.djspi.pipe01.fragment.PositionDialog;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;
import static java.lang.Double.valueOf;
import static java.lang.String.format;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS;
import static kr.djspi.pipe01.Const.REQUEST_CAPTURE_IMAGE;
import static kr.djspi.pipe01.Const.REQUEST_GALLERY;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.Const.TAG_PHOTO;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.URL_SPI;
import static kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.parseSpiType;

public class RegisterActivity extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    /**
     * 통합형은 위치 정보가 이미 있으므로 입력값 확인 과정 이후 위치정보 설정이 불필요하며,
     * 단일형의 경우 spiLocation 은 항상 null 로 시작한다.
     */
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private final static InputFilter[] FILTER_DEPTH = {new DecimalFilter(4, 2)};
    private static Spi spi;
    private static SpiType spiType;
    private static SpiMemo spiMemo;
    private static SpiPhoto spiPhoto;
    // TODO: 2019-04-11 통합형에서의 Spi* 클래스들의 정리 필요
    private static SpiLocation spiLocation;
    private static final Pipe pipe = new Pipe();
    private static final PipeType pipeType = new PipeType();
    private static final PipeShape pipeShape = new PipeShape();
    private static final PipePosition pipePosition = new PipePosition();
    private static final PipePlan pipePlan = new PipePlan();
    private static final PipeSupervise pipeSupervise = new PipeSupervise();
    private final Bundle superviseListBundle = new Bundle(1);
    private SpiPhotoObject photoObj;
    private ArrayList<String> superviseList;
    private TextView tHeader, tUnit;
    private LinearLayout lPhotoDesc;
    private ImageView imageThumb;
    private ImageView buttonEdit;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    FormEditText fPipe, fShape, fVertical, fHorizontal, fDepth, fSpec, fMaterial,
            fSupervise, fSuperviseContact, fMemo, fConstruction, fConstructionContact,
            fPhoto, fPhotoName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra("RegisterActivity");
        if (serializable instanceof HashMap) {
            HashMap<?, ?> itemMap = (HashMap) serializable;
            spi = (Spi) itemMap.get("Spi");
            spiType = (SpiType) itemMap.get("SpiType");
            spiLocation = (SpiLocation) itemMap.get("SpiLocation");
            spiMemo = (SpiMemo) itemMap.get("SpiMemo");
            spiPhoto = (SpiPhoto) itemMap.get("SpiPhoto");
        }
        setContentView(R.layout.activity_register);
        superviseList = getSuperviseList();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbarTitle(spiType.getType());

        findViewById(R.id.lay_pipe).setOnClickListener(this);
        fPipe = findViewById(R.id.form_pipe);
        fPipe.setOnClickListener(this);

        findViewById(R.id.lay_shape).setOnClickListener(this);
        fShape = findViewById(R.id.form_shape);
        fShape.setOnClickListener(this);
        fShape.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                pipeShape.setShape(s.toString());
            }
        });

        findViewById(R.id.lay_distance).setOnClickListener(this);
        fHorizontal = findViewById(R.id.form_horizontal);
        fHorizontal.setFocusable(false);
        fHorizontal.setOnClickListener(this);
        fVertical = findViewById(R.id.form_vertical);
        fVertical.setFocusable(false);
        fVertical.setOnClickListener(this);

        fDepth = findViewById(R.id.form_depth);
        fDepth.setFilters(FILTER_DEPTH);

        LinearLayout lSpec = findViewById(R.id.lay_spec);
        fSpec = findViewById(R.id.form_spec);
        tHeader = lSpec.findViewById(R.id.header);
        tUnit = lSpec.findViewById(R.id.unit);

        findViewById(R.id.lay_supervise).setOnClickListener(this);
        fSupervise = findViewById(R.id.form_supervise);
        fSupervise.setOnClickListener(this);
        fSuperviseContact = findViewById(R.id.form_supervise_contact);
        fSuperviseContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        fMaterial = findViewById(R.id.form_material);
        fMaterial.setOnEditorActionListener((v, actionId, event) -> {
            boolean isHandled = false;
            if (actionId == IME_ACTION_NEXT && fSupervise.getText().toString().equals("") && !superviseList.isEmpty()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(v.getWindowToken(), 0, 0);
                ListDialog listDialog = new ListDialog();
                listDialog.setArguments(superviseListBundle);
                listDialog.show(getSupportFragmentManager(), TAG_SUPERVISE);
                isHandled = true;
            }
            return isHandled;
        });

        fMemo = findViewById(R.id.form_memo);

        fConstruction = findViewById(R.id.form_construction);
        fConstructionContact = findViewById(R.id.form_construction_contact);
        fConstructionContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        findViewById(R.id.lay_photo).setOnClickListener(this);
        fPhoto = findViewById(R.id.form_photo);
        fPhoto.setOnClickListener(this);
        lPhotoDesc = findViewById(R.id.lay_photo_desc);
        lPhotoDesc.setOnClickListener(this);
        lPhotoDesc.setVisibility(GONE);
        imageThumb = lPhotoDesc.findViewById(R.id.form_photo_thumbnail);
        imageThumb.setOnClickListener(this);
        fPhotoName = lPhotoDesc.findViewById(R.id.form_photo_name);
        fPhotoName.setFocusable(false);
//        buttonEdit = lPhotoDesc.findViewById(R.id.btn_edit);
//        buttonEdit.setOnClickListener(this);
        ImageView buttonDelete = lPhotoDesc.findViewById(R.id.btn_delete);
        buttonDelete.setOnClickListener(this);

        findViewById(R.id.button_confirm).setOnClickListener(new OnNextButtonClick());
    }

    @Override
    void setToolbarTitle(String title) {
        if (title != null) {
            toolbar.setTitle(format(getString(R.string.app_title_alt), "매설관로", title));
        }
    }

    private ArrayList<String> getSuperviseList() {
        if (superviseList == null) {
            superviseList = new ArrayList<>();
            JsonObject jsonQuery = new JsonObject();
            jsonQuery.addProperty("json", "");
            Retrofit2x.builder()
                    .setService(new SuperviseGet(URL_SPI))
                    .setQuery(jsonQuery).build()
                    .run(new OnRetrofitListener() {
                        @Override
                        public void onResponse(JsonObject response) {
                            final JsonArray jsonArray = Json.a(response, "data");
                            for (JsonElement element : jsonArray) {
                                superviseList.add(Json.s(element.getAsJsonObject(), "supervise"));
                                superviseListBundle.putStringArrayList("superviseList", superviseList);
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "관리기관: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return superviseList;
        } else return superviseList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lay_pipe:
            case R.id.form_pipe:
                new ListDialog().show(getSupportFragmentManager(), TAG_PIPE);
                break;
            case R.id.lay_shape:
            case R.id.form_shape:
                pipeShape.setShape(null);
                new ListDialog().show(getSupportFragmentManager(), TAG_SHAPE);
                break;
            case R.id.lay_supervise:
            case R.id.form_supervise:
                if (superviseList.isEmpty()) {
                    fSupervise.setOnClickListener(null);
                    fSupervise.setEnabled(true);
                    fSupervise.setHint("직접 입력해주세요.");
                    return;
                }
                ListDialog listDialog = new ListDialog();
                listDialog.setArguments(superviseListBundle);
                listDialog.show(getSupportFragmentManager(), TAG_SUPERVISE);
                break;
            case R.id.lay_distance:
            case R.id.form_horizontal:
            case R.id.form_vertical:
                fHorizontal.setText(null);
                fVertical.setText(null);
                if (pipeShape.getShape() == null) {
                    new ListDialog().show(getSupportFragmentManager(), TAG_SHAPE);
                } else showPositionDialog();
                break;
            case R.id.lay_photo:
            case R.id.form_photo:
                lPhotoDesc.setVisibility(VISIBLE);
                new PhotoDialog().show(getSupportFragmentManager(), TAG_PHOTO);
                break;
            case R.id.form_photo_thumbnail:
                if (photoObj == null) return;
                ImageDialog imageDialog = new ImageDialog();
                Bundle bundle = new Bundle(1);
                bundle.putSerializable("SpiPhotoObject", photoObj);
                imageDialog.setArguments(bundle);
                imageDialog.show(getSupportFragmentManager(), TAG_PHOTO);
                break;
//            case R.id.btn_edit:
//                if (fPhotoName.getText().toString().equals("")) return;
//                fPhotoName.requestFocus();
//                break;
            case R.id.btn_delete:
                if (photoObj == null) return;
                photoObj.setUri(null);
                photoObj.setFile(null);
                if (tempFile != null && tempFile.delete()) {
                    tempFile = null;
                }
                tempUri = null;
                imageThumb.setImageDrawable(null);
                fPhotoName.setFocusable(false);
                fPhotoName.setText(getString(R.string.record_input_photo_delete));
                fPhotoName.setTextColor(getResources().getColor(R.color.colorAccent));
                fPhoto.setText(null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelect(String tag, int index, String... text) {
        if (index == -1) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        switch (tag) {
            case TAG_PIPE:
                fPipe.setText(PIPE_TYPE_ENUMS[index].getName());
                // TODO: 2019-04-01 index 번호 대신 서버에서 오는 정보 사용하도록 개선
                String header = PIPE_TYPE_ENUMS[index].getHeader();
                String unit = PIPE_TYPE_ENUMS[index].getUnit();
                tHeader.setText(format("%s  ", header));
                fSpec.setHint(format("%s 입력", header).replace("관경", "관로관경"));
                fSpec.setText(null);
                tUnit.setText(format("  %s", unit));
                pipe.setType_id(index + 1);
                pipeType.setId(index + 1);
                pipeType.setHeader(header);
                pipeType.setUnit(unit);
                if (unit.equals("mm")) fSpec.setInputType(TYPE_CLASS_NUMBER);
                else {
                    fSpec.setInputType(TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    fSpec.setError(null);
                }
                if (fShape.getText().toString().equals("")) {
                    pipeShape.setShape(null);
                    new ListDialog().show(getSupportFragmentManager(), TAG_SHAPE);
                } else return;
                break;
            case TAG_SHAPE:
                fShape.setText(PipeShapeEnum.values()[index].name());
                fHorizontal.setText(null);
                fVertical.setText(null);
                showPositionDialog();
                break;
            case TAG_SUPERVISE:
                fSupervise.setText(superviseList.get(index));
                pipeSupervise.setId(index + 1);
                pipe.setSupervise_id(index + 1);
                fSuperviseContact.requestFocus();
                break;
            case TAG_POSITION:
                pipePosition.setPosition(index);
                switch (index) {
                    case 1:
                        fHorizontal.setTag("좌측");
                        fVertical.setTag("전면");
                        break;
                    case 2:
                        fHorizontal.setTag("");
                        fVertical.setTag("전면");
                        break;
                    case 3:
                        fHorizontal.setTag("우측");
                        fVertical.setTag("전면");
                        break;
                    case 4:
                        fHorizontal.setTag("좌측");
                        fVertical.setTag("");
                        break;
                    case 5:
                        fHorizontal.setTag("직상");
                        fVertical.setTag("직상");
                        fHorizontal.setText("0.0");
                        fVertical.setText("0.0");
                        pipePosition.setHorizontal(0.0);
                        pipePosition.setVertical(0.0);
                        break;
                    case 6:
                        fHorizontal.setTag("우측");
                        fVertical.setTag("");
                        break;
                    case 7:
                        fHorizontal.setTag("좌측");
                        fVertical.setTag("후면");
                        break;
                    case 8:
                        fHorizontal.setTag("");
                        fVertical.setTag("후면");
                        break;
                    case 9:
                        fHorizontal.setTag("우측");
                        fVertical.setTag("후면");
                        break;
                    default:
                        fHorizontal.setTag("수평");
                        fVertical.setTag("수직");
                        break;
                }
                break;
            case TAG_DIRECTION:
                if (index == -2) { // 사용자가 이전 다이얼로그에서 '취소' 선택
                    showPositionDialog();
                    return;
                }
                pipePosition.setDirection(PIPE_DIRECTIONS[index]);
                pipePlan.setFile_plane(text[0] + ".png");
                break;
            case TAG_DISTANCE:
                if (index == -2) { // 사용자가 이전 다이얼로그에서 '취소' 선택
                    showPositionDialog();
                    return;
                }
                fHorizontal.setText(format("%s %s", fHorizontal.getTag().toString(), text[0]));
                fVertical.setText(format("%s %s", fVertical.getTag().toString(), text[1]));
                pipePosition.setHorizontal(valueOf(text[0]));
                pipePosition.setVertical(valueOf(text[1]));
                fDepth.requestFocus();
                imm.toggleSoftInput(SHOW_IMPLICIT, HIDE_NOT_ALWAYS);
                break;
            case TAG_PHOTO:
                switch (index) {
                    case 1:
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            try {
                                tempFile = ImageUtil.prepareFile();
                                tempUri = FileProvider.getUriForFile(this, packageName, tempFile);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                            } catch (IOException e) {
                                Toast.makeText(this, getString(R.string.record_photo_error), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
                        break;
                    case 2:
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                        galleryIntent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void showPositionDialog() {
        PositionDialog dialog = new PositionDialog();
        Bundle bundle = new Bundle();
        bundle.putString("typeString", spiType.getType());
        bundle.putString("shapeString", pipeShape.getShape());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), TAG_POSITION);
    }

    private File tempFile;
    private Uri tempUri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String imageFileName;
            switch (requestCode) {
                case REQUEST_CAPTURE_IMAGE:
                    photoObj = new SpiPhotoObject();
                    Glide.with(this).load(tempUri).thumbnail(0.25f).into(imageThumb);
                    imageFileName = tempFile.getName();
                    fPhotoName.setText(imageFileName);
                    fPhotoName.setTextColor(getResources().getColor(R.color.colorPrimary));
                    fPhoto.setText(getString(R.string.record_photo_ok));
                    photoObj.setUri(tempUri);
                    photoObj.setFile(ImageUtil.subSample4x(tempFile, 1024));
                    // TODO: 2019-04-12 갤러리 사진의 경우 원본이 바뀌면 안되므로 새 파일로 내부에서 처리하게 변경, 현재는 기존 파일을 덮어쓴다.
                    // TODO: 2019-04-12 단, 새로운 파일을 저장하는 것이 아닌, 메모리 내에서만 파일을 다룰 수 있게 한다.
                    tempUri = null;
                    tempFile = null;
                    break;
                case REQUEST_GALLERY:
                    photoObj = new SpiPhotoObject();
                    Uri imageFileUri = data.getData();
                    assert imageFileUri != null;
                    Glide.with(this).load(imageFileUri).thumbnail(0.25f).into(imageThumb);
                    imageFileName = ImageUtil.uriToFileName(this, imageFileUri);
                    fPhotoName.setText(imageFileName);
                    fPhotoName.setTextColor(getResources().getColor(R.color.colorPrimary));
                    fPhoto.setText(getString(R.string.record_photo_ok));
                    photoObj.setUri(imageFileUri);
                    photoObj.setFile(ImageUtil.uriToFile(this, imageFileUri));
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_CAPTURE_IMAGE:
                    try {
                        if (tempFile.delete()) tempFile = null;
                    } catch (SecurityException e) {
                        Toast.makeText(this, getString(R.string.record_delete_error), Toast.LENGTH_SHORT).show();
                    }
                    tempUri = null;
                    break;
                case REQUEST_GALLERY:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pipeShape.setShape(null);
        restoreInstanceState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInstanceState();
    }

    /**
     * 마지막 사용자 입력값 저장
     */
    private void saveInstanceState() {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("superviseContact", fSuperviseContact.getText().toString())
                .putString("material", fMaterial.getText().toString())
                .putString("construction", fConstruction.getText().toString())
                .putString("constructionContact", fConstructionContact.getText().toString());
        editor.apply();
    }

    /**
     * 마지막 사용자 입력값 불러오기
     */
    private void restoreInstanceState() {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        if (preferences != null) {
            fSuperviseContact.setText(preferences.getString("superviseContact", ""));
            fMaterial.setText(preferences.getString("material", ""));
            fConstruction.setText(preferences.getString("construction", ""));
            fConstructionContact.setText(preferences.getString("constructionContact", ""));
        }
    }

    private Entry setEntry() throws Exception {
        final int spiId = spi.getId();
        if (spiMemo == null) {
            spiMemo = new SpiMemo();
            spiMemo.setSpi_id(spiId);
        } else spiMemo.setSpi_id(spiId);
        spiMemo.setMemo(fMemo.getText().toString());
        if (spiPhoto == null) {
            spiPhoto = new SpiPhoto();
            spiPhoto.setSpi_id(spiId);
        } else spiPhoto.setSpi_id(spiId);
        if (spiLocation == null) {
            spiLocation = new SpiLocation();
            spiLocation.setSpi_id(spiId);
        } else spiLocation.setSpi_id(spiId);
        pipe.setSpi_id(spiId);
        pipe.setDepth(valueOf(fDepth.getText().toString()));
        pipe.setMaterial(fMaterial.getText().toString());
        pipe.setSupervise_contact(fSuperviseContact.getText().toString());
        pipe.setConstruction(fConstruction.getText().toString());
        pipe.setConstruction_contact(fConstructionContact.getText().toString());
        pipeType.setPipe(fPipe.getText().toString());
        pipeShape.setShape(fShape.getText().toString());
        pipeShape.setSpec(fSpec.getText().toString());
        pipePlan.setFile_section(format("plan_%s_%s.png", parseSpiType(spiType.getType()), pipePosition.getPosition()));
        pipeSupervise.setSupervise(fSupervise.getText().toString());

        Entry entry = new Entry(spi, spiType, spiMemo, spiPhoto, pipe, pipeType, pipeShape, pipePosition, pipePlan, pipeSupervise);
        entry.setSpi_location(spiLocation);

        return entry;
    }

    private final class OnNextButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (isAllValid() && isSpecValid()) try {
                final Entry entry = setEntry();
                ArrayList<Entry> previewEntries = new ArrayList<>();
                previewEntries.add(entry);
                startActivity(new Intent(getApplicationContext(), ViewActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("RegisterPreview", previewEntries)
                        .putExtra("PipeIndex", 0)
                        .putExtra("fHorizontal", fHorizontal.getText().toString())
                        .putExtra("fVertical", fVertical.getText().toString())
                        .putExtra("SpiPhotoObject", photoObj));
            } catch (Exception e) {
                showMessageDialog(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.", true);
                Log.e(TAG, e.getMessage());
            }
        }

        private boolean isAllValid() {
            boolean allValid = true;
            final FormEditText[] validateFields
                    = {fPipe, fShape, fHorizontal, fVertical, fDepth, fSpec, fMaterial, fSupervise, fSuperviseContact};
            for (FormEditText field : validateFields) {
                allValid = field.testValidity() && allValid;
            }
            return allValid;
        }

        private boolean isSpecValid() {
            boolean isSpecValid = true;
            if (fSpec.getInputType() == TYPE_CLASS_NUMBER) {
                isSpecValid = Double.valueOf(fSpec.getText().toString()) < 1000.0;
                if (!isSpecValid) fSpec.setError("이 범위(0.0 - 1000.0)안에 해당하는 숫자만 입력가능합니다.");
            }
            return isSpecValid;
        }
    }
}
