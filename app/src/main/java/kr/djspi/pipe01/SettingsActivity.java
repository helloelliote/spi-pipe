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

import kr.djspi.pipe01.fragment.ListDialog;
import kr.djspi.pipe01.fragment.OnSelectListener;

import static kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;

public class SettingsActivity extends AppCompatActivity implements OnSelectListener {

    private SettingsFragment settingsFragment;

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

    @Override
    public void onSelect(String tag, int index, @Nullable String... text) {
        if (index == -1) return;
        switch (tag) {
            case TAG_PIPE:
                runOnUiThread(() -> settingsFragment.getPipeTypePref().setSummary(PIPE_TYPE_ENUMS[index].getName()));
                break;
            case TAG_SUPERVISE:
                if (text == null) return;
                runOnUiThread(() -> settingsFragment.getSupervisePref().setSummary(text[0]));
                break;
            default:
                break;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Preference pipeTypePref;
        private Preference supervisePref;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey);

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
                pipeTypePref.setPersistent(true);
                pipeTypePref.setOnPreferenceClickListener(preference -> {
                    new ListDialog().show(getChildFragmentManager(), TAG_PIPE);
                    return false;
                });
            }
            // TODO: 2019-05-12 설정값이 저장되지 않음

            supervisePref = findPreference("supervise");
            if (supervisePref != null) {
                supervisePref.setPersistent(true);
                supervisePref.setOnPreferenceClickListener(preference -> {
                    new ListDialog().show(getChildFragmentManager(), TAG_SUPERVISE);
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
