package kr.djspi.pipe01;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;
import com.helloelliote.util.retrofit.Retrofit2x;
import com.helloelliote.util.retrofit.RetrofitCore;
import com.helloelliote.util.retrofit.SuperviseGet;

import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;
import kr.djspi.pipe01.sql.Supervise;
import kr.djspi.pipe01.sql.SuperviseDao;

import static kr.djspi.pipe01.BaseActivity.superviseDb;
import static kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.Const.URL_SPI;

public class SettingsActivity extends AppCompatActivity implements OnSelectListener {

    private SettingsFragment settingsFragment;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsFragment = new SettingsFragment();
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreInstanceState();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    private void restoreInstanceState() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        runOnUiThread(() -> {
            String pipeType = sharedPreferences.getString("pipe_type", null);
            settingsFragment.getPipeTypePref().setSummary(pipeType);
            String supervise = sharedPreferences.getString("supervise", null);
            settingsFragment.getSupervisePref().setSummary(supervise);
        });
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

    @Override
    public void onSelect(String tag, int index, @Nullable String... text) {
        if (index == -1) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (tag) {
            case TAG_PIPE:
                runOnUiThread(() -> settingsFragment.getPipeTypePref().setSummary(PIPE_TYPE_ENUMS[index].getName()));
                editor.putString("pipe_type", PIPE_TYPE_ENUMS[index].getName());
                editor.putInt("pipe_type_id", index);
                editor.apply();
                break;
            case TAG_SUPERVISE:
                if (text == null) return;
                runOnUiThread(() -> settingsFragment.getSupervisePref().setSummary(text[0]));
                editor.putString("supervise", text[0]);
                editor.apply();
                break;
            default:
                break;
        }
    }

    static class SettingsFragment extends PreferenceFragmentCompat {

        private Preference pipeTypePref;
        private Preference supervisePref;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey);

            Preference helpPref = findPreference("help");
            if (helpPref != null && getContext() != null) {
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
                    JsonObject jsonQuery = new JsonObject();
                    jsonQuery.addProperty("json", "");
                    Retrofit2x.builder()
                            .setService(new SuperviseGet(URL_SPI))
                            .setQuery(jsonQuery).build()
                            .run(new RetrofitCore.OnRetrofitListener() {

                                @Override
                                public void onResponse(JsonObject response) {
                                    new Thread(() -> {
                                        final JsonArray jsonArray = Json.a(response, "data");
                                        SuperviseDao superviseDao = superviseDb.dao();
                                        for (JsonElement element : jsonArray) {
                                            JsonObject object = element.getAsJsonObject();
                                            superviseDao.insert(new Supervise(Json.i(object, "id"), Json.s(object, "supervise")));
                                        }
                                    }).start();
                                    new ListDialog().show(getChildFragmentManager(), TAG_SUPERVISE);
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                }
                            });
                    return false;
                });
            }
        }

        Preference getPipeTypePref() {
            return pipeTypePref;
        }

        Preference getSupervisePref() {
            return supervisePref;
        }
    }
}
