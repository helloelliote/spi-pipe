package kr.djspi.pipe01;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore.OnRetrofitListener;
import com.helloelliote.util.retrofit.SuperviseGet;

import java.util.ArrayList;
import java.util.HashMap;

import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;

import static kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.URL_SPI;

// TODO: 2019-05-10 관리기관 목록 가져오기
public class SettingsActivity extends AppCompatActivity implements OnSelectListener {

    private ArrayList<String> superviseList;
    private HashMap<String, Integer> superviseMap;
    private static SettingsFragment settingsFragment = new SettingsFragment();
    private static final Bundle superviseListBundle = new Bundle(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        superviseList = getSuperviseList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(null);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events
        }
    }

    private ArrayList<String> getSuperviseList() {
        if (superviseList == null || superviseMap == null) {
            superviseList = new ArrayList<>();
            superviseMap = new HashMap<>();
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
                                JsonObject object = element.getAsJsonObject();
                                superviseMap.put(Json.s(object, "supervise"), Json.i(object, "id"));
                                superviseList.add(Json.s(object, "supervise"));
                                superviseListBundle.putStringArrayList("superviseList", superviseList);
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                        }
                    });
            return superviseList;
        } else return superviseList;
    }

    @Override
    public void onSelect(String tag, int index, @Nullable String... text) {
        if (index == -1) return;
        switch (tag) {
            case TAG_PIPE:
                settingsFragment.pipeTypePref.setSummary(PIPE_TYPE_ENUMS[index].getName());
                break;
            case TAG_SUPERVISE:
                // TODO: 2019-05-10 관로종류 및 관리기관 정보 넘겨주기
                break;
            default:
                break;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        Preference pipeTypePref;
        Preference supervisePref;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference helpPref = findPreference("help");
            if (helpPref != null) {
                helpPref.setOnPreferenceClickListener(preference -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog alertDialog = builder.setIcon(R.drawable.ic_help)
                            .setTitle("관로정보 일괄적용")
                            .setMessage(getString(R.string.message_preset_help))
                            .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss()).create();
                    alertDialog.show();
                    return false;
                });
            }

            pipeTypePref = findPreference("pipe_type");
            if (pipeTypePref != null) {
                pipeTypePref.setOnPreferenceClickListener(preference -> {
                    new ListDialog().show(getChildFragmentManager(), TAG_PIPE);
                    return false;
                });

            }

            supervisePref = findPreference("supervise");
            if (supervisePref != null) {
                supervisePref.setOnPreferenceClickListener(preference -> {
                    ListDialog listDialog = new ListDialog();
                    listDialog.setArguments(superviseListBundle);
                    listDialog.show(getChildFragmentManager(), TAG_SUPERVISE);
                    return false;
                });
            }
        }
    }
}
