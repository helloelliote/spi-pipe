package kr.djspi.pipe01;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.helloelliote.json.Json;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

import kr.djspi.pipe01.dto.Entry;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.tab.OnRecordListener;
import kr.djspi.pipe01.tab.TabAdapter;

import static android.content.Intent.ACTION_DIAL;
import static android.net.Uri.parse;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Objects.requireNonNull;
import static kr.djspi.pipe01.Const.REQUEST_CODE_MAP;
import static kr.djspi.pipe01.Const.RESULT_FAIL;
import static kr.djspi.pipe01.Const.RESULT_PASS;

public class ViewActivity extends BaseActivity implements Serializable, OnRecordListener {

    private static final String TAG = ViewActivity.class.getSimpleName();
    private int pipeIndex;
    private JsonObject jsonObject;
    private ArrayList<Entry> previewEntries;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    ViewPager viewPager;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewEntries = new ArrayList<>();

        final Intent intent = getIntent();
        if (intent != null) {
            String jsonString = intent.getStringExtra("PipeView");
            if (jsonString != null) {
                jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            }

            Serializable serializable = intent.getSerializableExtra("RegisterPreview");
            pipeIndex = intent.getIntExtra("PipeIndex", 0);
            String fHorizontal = intent.getStringExtra("fHorizontal");
            String fVertical = intent.getStringExtra("fVertical");
            if (serializable instanceof ArrayList<?>) {
                previewEntries = (ArrayList<Entry>) serializable;
                jsonObject = new JsonObject();
                jsonObject = parseEntry(previewEntries, pipeIndex, fHorizontal, fVertical); // 단일형 index 는 항상 0
            }
        }
        setContentView(R.layout.activity_pipe_view);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // (탭) 사용자의 좌우 스와이프 이벤트를 처리가능하게끔 어댑터를 사용
        TabLayout tabLayout = findViewById(R.id.tabs);
        if (previewEntries.isEmpty()) {
            tabLayout.removeTab(requireNonNull(tabLayout.getTabAt(3)));
        }
        final TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabSelected());

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(getResources().getColor(R.color.yellow));
        drawable.setSize(8, 1);
        linearLayout.setDividerDrawable(drawable);

        setToolbarTitle("");

        setSpiIdInfo();
        setSuperviseInfo();
        setConstructionInfo();
    }

    @Override
    void setToolbarTitle(String string) {
        if (string != null) {
            toolbar.setTitle(String.format(getString(R.string.app_title_alt), Json.s(jsonObject, "pipe"), ""));
        }
    }

    private void setSpiIdInfo() {
        TextView id = findViewById(R.id.txt_id);
        try {
            String idInt = Json.s(jsonObject, "spi_id");
            id.setText(fromHtml(getString(R.string.nfc_info_id, idInt)));
        } catch (RuntimeException e) {
            id.setVisibility(GONE);
        }
    }

    private void setSuperviseInfo() {
        TextView name = findViewById(R.id.txt_company);
        TextView contact = findViewById(R.id.txt_contact);
        try {
            String superviseName = Json.s(jsonObject, "supervise");
            name.setText(fromHtml(getString(R.string.nfc_info_company, superviseName)));

            String superviseContact = Json.s(jsonObject, "supervise_contact");
            contact.setText(fromHtml(getString(R.string.nfc_info_company_contact, superviseContact)));
            contact.setOnClickListener(v -> startActivity(new Intent(ACTION_DIAL, parse("tel:" + superviseContact))));
        } catch (RuntimeException e) {
            name.setVisibility(GONE);
            contact.setVisibility(GONE);
        }
    }

    private void setConstructionInfo() {
        TextView name = findViewById(R.id.txt_construction);
        name.setVisibility(GONE);
        try {
            String constructionName = Json.s(jsonObject, "construction");
            String constructionContact = Json.s(jsonObject, "construction_contact");
            if (constructionName.length() > 0 || constructionContact.length() > 0) {
                name.setVisibility(VISIBLE);
                name.setText(fromHtml(getString(R.string.nfc_info_construction, constructionName, constructionContact)));
                if (constructionContact.length() > 0) {
                    name.setOnClickListener(v -> startActivity(new Intent(ACTION_DIAL, parse("tel:" + constructionContact))));
                }
            }
        } catch (RuntimeException e) {
            name.setVisibility(GONE);
        }
    }

    @Override
    public JsonObject getJsonObjectRecord() {
        return jsonObject;
    }

    @Override
    public void onRecord(String tag, int result) {
        switch (result) {
            case RESULT_PASS:
                startActivityForResult(new Intent(this, SpiLocationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQUEST_CODE_MAP);
                break;
            case RESULT_FAIL:
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_MAP:
                    double[] locations = data.getDoubleArrayExtra("locations");
                    Entry currentEntry = previewEntries.get(pipeIndex);
                    SpiLocation location = currentEntry.getSpi_location();
                    location.setLatitude(locations[0]);
                    location.setLongitude(locations[1]);
                    location.setCount(0);
                    currentEntry.setSpi_location(location);
                    previewEntries.set(pipeIndex, currentEntry);
                    startActivity(new Intent(this, SpiPostActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("entry", previewEntries));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcUtil.onPause();
    }

    private class TabSelected implements OnTabSelectedListener {

        @Override
        public void onTabSelected(@NonNull Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
            LinearLayout layout = findViewById(R.id.lay_bottom);
            if (tab.getPosition() == 3) layout.setVisibility(GONE);
            else layout.setVisibility(VISIBLE);
        }

        @Override
        public void onTabUnselected(Tab tab) {

        }

        @Override
        public void onTabReselected(Tab tab) {

        }
    }

    @SuppressWarnings("SameParameterValue")
    private static JsonObject parseEntry(@NotNull ArrayList entries, int index, String... strings) {
        return ((Entry) entries.get(index)).parseToSingleJsonObject(strings);
    }
}
