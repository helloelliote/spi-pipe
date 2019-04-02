package kr.djspi.pipe01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.json.Json;
import com.helloelliote.retrofit.Retrofit2x;
import com.helloelliote.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.retrofit.SuperviseGet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
import kr.djspi.pipe01.dto.SpiType;
import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.fragment.PositionDialog;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
import static android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY;
import static android.view.inputmethod.InputMethodManager.SHOW_FORCED;
import static kr.djspi.pipe01.Const.PIPE_DIRECTIONS;
import static kr.djspi.pipe01.Const.REQUEST_CODE_GALLERY;
import static kr.djspi.pipe01.Const.REQUEST_CODE_MAP;
import static kr.djspi.pipe01.Const.REQUEST_CODE_PHOTO;
import static kr.djspi.pipe01.Const.TAG_DIRECTION;
import static kr.djspi.pipe01.Const.TAG_DISTANCE;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_POSITION;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.URL_SPI;

public class RecordInputActivity2 extends BaseActivity implements OnSelectListener, OnClickListener, Serializable {

    private static final String TAG = RecordInputActivity2.class.getSimpleName();
    public static final PipeShapeEnum[] shapes = PipeShapeEnum.values();
    public static FragmentManager fragmentManager;
    private static Spi spi;
    private static SpiType spiType;
    private static SpiMemo spiMemo;
    private static final Pipe pipe = new Pipe();
    private static final PipeType pipeType = new PipeType();
    private static final PipeShape pipeShape = new PipeShape();
    private static final PipePosition pipePosition = new PipePosition();
    private static final PipePlan pipePlan = new PipePlan();
    private static final PipeSupervise pipeSupervise = new PipeSupervise();
    private TextView tHeader, tUnit;
    private Bundle superviseListBundle = new Bundle(1);
    public ArrayList<String> superviseList;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static SpiLocation spiLocation;
    static FormEditText fPipe, fShape, fVertical, fHorizontal, fDepth, fSpec, fMaterial,
            fSupervise, fSuperviseContact, fMemo, fConstruction, fConstructionContact;
    static File mPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra("PipeRecordActivity2");
        if (serializable instanceof HashMap) {
            HashMap<?, ?> itemMap = (HashMap) serializable;
            spi = (Spi) itemMap.get("Spi");
            spiType = (SpiType) itemMap.get("SpiType");
            spiLocation = (SpiLocation) itemMap.get("SpiLocation");
            spiMemo = (SpiMemo) itemMap.get("SpiMemo");
        }
        setContentView(R.layout.activity_record_input_2);
        superviseList = getSuperviseList();
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            final FormEditText[] allFields
                    = {fPipe, fShape, fVertical, fHorizontal, fDepth, fSpec, fMaterial,
                    fSupervise, fSuperviseContact, fMemo, fConstruction, fConstructionContact};
            for (FormEditText field : allFields) {
                field.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/nanumsquareroundr.ttf"));
            }
        }

        findViewById(R.id.button_confirm).setOnClickListener(new OnNextButtonClick());
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
            jsonQuery.addProperty("com/helloelliote/json", "");
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
                            Toast.makeText(context, "관리기관: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (pipeShape.getShape() == null) {
                    new ListDialog().show(getSupportFragmentManager(), TAG_SHAPE);
                } else showPositionDialog();
                break;
            default:
                break;
        }
    }

    public static void showPositionDialog() {
        PositionDialog dialog = new PositionDialog();
        Bundle bundle = new Bundle();
        bundle.putString("typeString", spiType.getType());
        bundle.putString("shapeString", pipeShape.getShape());
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, TAG_POSITION);
    }

    @Override
    public void onSelect(String tag, int index, String... text) {
        if (index == -1) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        switch (tag) {
            case TAG_PIPE:
                // TODO: 2019-04-01 index 번호 대신 서버에서 오는 정보 사용하도록 개선
                fPipe.setText(pipes[index].getName());
                pipeType.setId(index + 1);
                pipe.setType_id(index + 1);
                String header = pipes[index].getHeader();
                tHeader.setText(String.format("%s  ", header));
                fSpec.setHint(String.format("%s 입력", header).replace("관경", "관로관경"));
                String unit = pipes[index].getUnit();
                tUnit.setText(String.format("  %s", unit));
                if (unit.equals("mm")) fSpec.setInputType(TYPE_CLASS_NUMBER);
                else fSpec.setInputType(TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                pipeShape.setShape(null);
                new ListDialog().show(getSupportFragmentManager(), TAG_SHAPE);
                break;
            case TAG_SHAPE:
                fShape.setText(shapes[index].name());
                break;
            case TAG_SUPERVISE:
                fSupervise.setText(superviseList.get(index));
                pipeSupervise.setId(index + 1);
                pipe.setSupervise_id(index + 1);
                fSuperviseContact.requestFocus();
                imm.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY);
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
                pipePosition.setDirection(PIPE_DIRECTIONS[index]);
                pipePlan.setFile_plane(text[0] + ".png");
                break;
            case TAG_DISTANCE:
                fHorizontal.setText(String.format("%s %s", fHorizontal.getTag().toString(), text[0]));
                fVertical.setText(String.format("%s %s", fVertical.getTag().toString(), text[1]));
                pipePosition.setHorizontal(Double.valueOf(text[0]));
                pipePosition.setVertical(Double.valueOf(text[1]));
                fDepth.requestFocus();
                imm.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY);
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
                if (mPhoto.delete()) mPhoto = null;
            }
            switch (requestCode) {
                case REQUEST_CODE_PHOTO:
//                    onPhotoInput.getPhoto();
                    break;
                case REQUEST_CODE_GALLERY:
//                    onPhotoInput.getGallery(data.getData());
                    break;
                case REQUEST_CODE_MAP:
                    double[] locations = data.getDoubleArrayExtra("locations");
                    spiLocation.setLatitude(locations[0]);
                    spiLocation.setLongitude(locations[1]);
                    spiLocation.setCount(0);
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
        restoreState();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    /**
     * 마지막 사용자 입력값 저장
     */
    private void saveState() {
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
    private void restoreState() {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        if (preferences != null) {
            fSuperviseContact.setText(preferences.getString("superviseContact", ""));
            fMaterial.setText(preferences.getString("material", ""));
            fConstruction.setText(preferences.getString("construction", ""));
            fConstructionContact.setText(preferences.getString("constructionContact", ""));
        }
    }

    private class OnNextButtonClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (spiLocation == null) { // 아직 위치 정보가 기록되지 않음
                spiLocation = new SpiLocation();
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
                showMessageDialog(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.", true);
                Log.e(TAG, e.getMessage());
            }
        }

        // TODO: 2019-03-26 위치 정보 기록 전에 모두 체크하고 넘기게, 체크 실패 시 다이얼로그 출력
        private boolean isAllValid() {
            boolean allValid = true;
            final FormEditText[] validateFields
                    = {fPipe, fShape, fHorizontal, fVertical, fDepth, fSpec, fMaterial, fSupervise, fSuperviseContact};
            for (FormEditText field : validateFields) {
                allValid = field.testValidity() && allValid;
            }
            return allValid;
        }
    }

    @NotNull
    @Contract(" -> new")
    private static Entry setEntry() throws Exception {
        final int spiId = spi.getId();
        if (spiMemo == null) spiMemo = new SpiMemo();
        spiMemo.setSpi_id(spiId);
        spiMemo.setMemo(fMemo.getText().toString());
        spiLocation.setSpi_id(spiId);
        pipe.setSpi_id(spiId);
        pipe.setDepth(Double.valueOf(fDepth.getText().toString()));
        pipe.setMaterial(fMaterial.getText().toString());
        pipeShape.setShape(fShape.getText().toString());
        pipeShape.setSpec(fSpec.getText().toString());
        pipeType.setPipe(fPipe.getText().toString());
        pipeSupervise.setSupervise(fSupervise.getText().toString());
        pipe.setSupervise_contact(fSuperviseContact.getText().toString());
        pipe.setConstruction(fConstruction.getText().toString());
        pipe.setConstruction_contact(fConstructionContact.getText().toString());

        return new Entry(
                spi, spiType, spiLocation, spiMemo,
                pipe, pipeType, pipeShape, pipePosition, pipePlan, pipeSupervise);
    }
}
