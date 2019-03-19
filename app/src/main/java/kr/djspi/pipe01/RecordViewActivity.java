package kr.djspi.pipe01;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

import kr.djspi.pipe01.tab.OnRecordListener;
import kr.djspi.pipe01.tab.TabAdapter;

import static android.net.Uri.parse;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RecordViewActivity extends BaseActivity implements Serializable, OnRecordListener {

    private static final String TAG = RecordViewActivity.class.getSimpleName();
    private static final String ACTION_DIAL = "android.intent.action.DIAL";
    private static JsonObject jsonObject;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonString = getIntent().getStringExtra("RecordViewActivity");
        if (jsonString != null) jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        setContentView(R.layout.activity_record_view);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // (탭) 사용자의 좌우 스와이프 이벤트를 처리가능하게끔 어댑터를 사용
        TabLayout tabLayout = findViewById(R.id.tabs);
        final TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabSelected());

        setToolbarTitle("");

        setSuperviseInfo();
        setConstructionInfo();
//        setSerialInfo();
    }

    @Override
    void setToolbarTitle(String string) {
        if (string != null) {
            toolbar.setTitle(String.format("%s %s",
                    jsonObject.get("spi_id").getAsString(),
                    jsonObject.get("pipe").getAsString()));
        }
    }

    private void setSuperviseInfo() {
        TextView name = findViewById(R.id.txt_company);
        TextView contact = findViewById(R.id.txt_contact);
        try {
            String superviseName = jsonObject.get("supervise").getAsString();
            name.setText(fromHtml(getString(R.string.nfc_info_company, superviseName)));

            String superviseContact = jsonObject.get("supervise_contact").getAsString();
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
            String constructionName = jsonObject.get("construction").getAsString();
            String constructionContact = jsonObject.get("construction_contact").getAsString();

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

    private void setSerialInfo() {
        TextView serial = findViewById(R.id.txt_serial);
        try {
            String spiId = jsonObject.get("spi_id").getAsString();
            serial.setText(fromHtml(getString(R.string.nfc_info_id, spiId)));
        } catch (RuntimeException e) {
            serial.setVisibility(GONE);
        }
    }

    @Override
    public JsonObject getJsonObjectRecord() {
        return jsonObject;
    }

    private class TabSelected implements OnTabSelectedListener {

        @Override
        public void onTabSelected(@NonNull Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab) {

        }

        @Override
        public void onTabReselected(Tab tab) {

        }
    }
}
