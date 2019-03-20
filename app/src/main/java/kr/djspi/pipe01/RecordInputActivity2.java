package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.FragmentManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.PositionDialog;
import kr.djspi.pipe01.retrofit2x.Retrofit2x;
import kr.djspi.pipe01.retrofit2x.RetrofitCore.OnRetrofitListener;
import kr.djspi.pipe01.retrofit2x.SuperviseGet;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
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

public class RecordInputActivity2 extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    private static final String TAG = RecordInputActivity2.class.getSimpleName();
    public static final PipeShapeEnum[] shapes = PipeShapeEnum.values();
    public static FragmentManager fragmentManager;
    public static ArrayList<String> superviseList;
    private static HashMap<?, ?> itemMap;
    private static SpiType spiType;
    private static final Pipe pipe = new Pipe();
    private static final PipeType pipeType = new PipeType();
    private static final PipeShape pipeShape = new PipeShape();
    private static final PipePosition pipePosition = new PipePosition();
    private static final PipePlan pipePlan = new PipePlan();
    private static final PipeSupervise pipeSupervise = new PipeSupervise();
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static final SpiLocation spiLocation = new SpiLocation();
    static FormEditText fPipe, fShape, fVertical, fHorizontal, fDepth, fSpec, fMaterial,
            fSupervise, fSuperviseContact, fMemo, fConstruction, fConstructionContact;
    TextView tHeader, tUnit;
    MaterialButton buttonConfirm;
    static File mPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        Serializable serializable = getIntent().getSerializableExtra("PipeRecordActivity");
        if (serializable instanceof HashMap) {
            itemMap = (HashMap) serializable;
            spiType = (SpiType) Objects.requireNonNull(itemMap.get("spiType"));
        }
        setContentView(R.layout.activity_record_input_2);
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
        fVertical = findViewById(R.id.form_vertical);
        fHorizontal = findViewById(R.id.form_horizontal);

        fDepth = findViewById(R.id.form_depth);

        LinearLayout lSpec = findViewById(R.id.lay_spec);
        fSpec = findViewById(R.id.form_spec);
        tHeader = lSpec.findViewById(R.id.header);
        tUnit = lSpec.findViewById(R.id.unit);

        fMaterial = findViewById(R.id.form_material);

        findViewById(R.id.lay_supervise).setOnClickListener(this);
        fSupervise = findViewById(R.id.form_supervise);
        fSupervise.setOnClickListener(this);
        fSuperviseContact = findViewById(R.id.form_supervise_contact);
        fSuperviseContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        fMemo = findViewById(R.id.form_memo);

        fConstruction = findViewById(R.id.form_construction);
        fConstructionContact = findViewById(R.id.form_construction_contact);
        fConstructionContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        buttonConfirm = findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(new OnNextButtonClick());
    }

    @Override
    void setToolbarTitle(String title) {
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
        if (ListDialog.get().isAdded()) return;
        switch (v.getId()) {
            case R.id.lay_pipe:
            case R.id.form_pipe:
                ListDialog.get().show(fragmentManager, TAG_PIPE);
                break;
            case R.id.lay_shape:
            case R.id.form_shape:
                pipeShape.setShape(null);
                ListDialog.get().show(fragmentManager, TAG_SHAPE);
                break;
            case R.id.lay_supervise:
            case R.id.form_supervise:
                if (superviseList.isEmpty()) {
                    fSupervise.setOnClickListener(null);
                    fSupervise.setEnabled(true);
                    fSupervise.setHint("직접 입력해주세요.");
                    return;
                }
                ListDialog.get().show(fragmentManager, TAG_SUPERVISE);
                break;
            case R.id.lay_distance:
                if (pipeShape.getShape() == null) {
                    Toast.makeText(context, "관로형태를 먼저 입력해 주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                showPositionDialog();
                break;
            default:
                break;
        }
    }

    public static void showPositionDialog() {
        PositionDialog dialog = new PositionDialog();
        Bundle bundle = new Bundle(3);
        bundle.putString("typeString", spiType.getType());
        bundle.putString("shapeString", pipeShape.getShape());
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, TAG_POSITION);
    }

    @Override
    public void onSelect(String tag, int index, String text) {
        if (index == -1) return;
        switch (tag) {
            case TAG_PIPE:
                fPipe.setText(pipes[index].getName());
                pipeType.setId(index + 1);
                pipe.setType_id(index + 1);
                String header = pipes[index].getHeader();
                tHeader.setText(header + "  ");
                String unit = pipes[index].getUnit();
                tUnit.setText("  " + unit);
                fSpec.setInputType(index == 5 ? TYPE_CLASS_TEXT : TYPE_CLASS_NUMBER); // index == 5 : 통신관로
                break;
            case TAG_SHAPE:
                fShape.setText(shapes[index].name());
                break;
            case TAG_SUPERVISE:
                fSupervise.setText(superviseList.get(index));
                pipeSupervise.setId(index + 1);
                pipe.setSupervise_id(index + 1);
                break;
            case TAG_TYPE_PLATE:
            case TAG_TYPE_MARKER:
            case TAG_TYPE_COLUMN:
                pipePosition.setPosition(index);
                break;
            case TAG_POSITION:
                pipePosition.setPosition(index);
                switch (index) {
                    case 1:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("좌측");
                        fVertical.setEnabled(true);
                        fVertical.setHint("차도방향");
                        break;
                    case 2:
                        fHorizontal.setEnabled(false);
                        fHorizontal.setHint("없음");
                        fHorizontal.setText("0");
                        fVertical.setEnabled(true);
                        fVertical.setHint("차도방향");
                        break;
                    case 3:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("우측");
                        fVertical.setEnabled(true);
                        fVertical.setHint("차도방향");
                        break;
                    case 4:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("좌측");
                        fVertical.setEnabled(false);
                        fVertical.setHint("없음");
                        fVertical.setText("0");
                        break;
                    case 5:
                        fHorizontal.setEnabled(false);
                        fHorizontal.setHint("없음");
                        fHorizontal.setText("0");
                        fVertical.setEnabled(false);
                        fVertical.setHint("없음");
                        fVertical.setText("0");
                        break;
                    case 6:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("우측");
                        fVertical.setEnabled(false);
                        fVertical.setHint("없음");
                        fVertical.setText("0");
                        break;
                    case 7:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("좌측");
                        fVertical.setEnabled(true);
                        fVertical.setHint("보도방향");
                        break;
                    case 8:
                        fHorizontal.setEnabled(false);
                        fHorizontal.setHint("없음");
                        fHorizontal.setText("0");
                        fVertical.setEnabled(true);
                        fVertical.setHint("보도방향");
                        break;
                    case 9:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("우측");
                        fVertical.setEnabled(true);
                        fVertical.setHint("보도방향");
                        break;
                    default:
                        fHorizontal.setEnabled(true);
                        fHorizontal.setHint("수평");
                        fVertical.setEnabled(true);
                        fVertical.setHint("수직");
                        break;
                }
                break;
            case TAG_DIRECTION:
                pipePosition.setDirection(PIPE_DIRECTIONS[index]);
                pipePlan.setFile_plane(text + ".png");
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
        spiLocation.setCount(-1);
        super.onPause();
    }

    private class OnNextButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (spiLocation.getCount() != 1) { // 아직 위치 정보가 기록되지 않음
                startActivityForResult(new Intent(context, SpiLocationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQUEST_CODE_MAP);
            } else if (isAllValid()) try {
                final Entry entry = setEntry();
                ArrayList<Entry> entries = new ArrayList<>(1);
                entries.add(entry);
                startActivity(new Intent(context, RecordWriteActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("entry", entries));
            } catch (Exception e) {
                showMessageDialog(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.");
                e.printStackTrace();
            }
        }

        private boolean isAllValid() {
            boolean allValid = true;
            final FormEditText[] allFields
                    = new FormEditText[]{fPipe, fShape, fHorizontal, fVertical, fDepth, fSpec, fMaterial, fSupervise, fSuperviseContact};
            for (FormEditText field : allFields) {
                allValid = field.testValidity() && allValid;
            }
            return allValid;
        }
    }

    @NotNull
    @Contract(" -> new")
    private static Entry setEntry() throws Exception {
        Spi spi = (Spi) itemMap.get("spi");
        final int spiId = Objects.requireNonNull(spi).getId();
        SpiMemo spiMemo = new SpiMemo(fMemo.getText().toString());
        spiMemo.setSpi_id(spiId);
        spiLocation.setSpi_id(spiId);
        pipe.setSpi_id(spiId);
        pipe.setDepth(Double.valueOf(fDepth.getText().toString()));
        pipe.setMaterial(fMaterial.getText().toString());
        pipeShape.setShape(fShape.getText().toString());
        pipeShape.setSpec(fSpec.getText().toString());
        pipeType.setPipe(fPipe.getText().toString());
        pipePosition.setHorizontal(Double.valueOf(fHorizontal.getText().toString()));
        pipePosition.setVertical(Double.valueOf(fVertical.getText().toString()));
        pipeSupervise.setSupervise(fSupervise.getText().toString());
        pipe.setSupervise_contact(fSuperviseContact.getText().toString());
        pipe.setConstruction(fConstruction.getText().toString());
        pipe.setConstruction_contact(fConstructionContact.getText().toString());

        return new Entry(
                spi, spiType, spiLocation, spiMemo,
                pipe, pipeType, pipeShape, pipePosition, pipePlan, pipeSupervise);
    }
}