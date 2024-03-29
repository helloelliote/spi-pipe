package kr.djspi.pipe01

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.util.onNewIntentIgnore
import kr.djspi.pipe01.util.onPauseNfc
import kr.djspi.pipe01.util.onResumeNfc
import kr.djspi.pipe01.util.updateLocalSuperviseDatabase

class SettingsActivity : BaseActivity(), OnSelectListener {

    private lateinit var preferences: SharedPreferences
    private var settingsFragment: SettingsFragment

    init {
        settingsFragment = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction().replace(R.id.settings, settingsFragment).commit()
        preferences = AppPreference.defaultPrefs(this)
        nmap_find.visibility = View.GONE
        setting_confirm.visibility = View.VISIBLE
        setting_confirm.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        if (index == -1) return
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentIgnore()
    }

    open class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey)
            findPreference<Preference>("help")?.let {
                it.setOnPreferenceClickListener {
                    AlertDialog.Builder(requireContext())
                        .setIcon(R.drawable.ic_help)
                        .setTitle("입력내용 일괄적용")
                        .setMessage(getString(R.string.message_preset_help))
                        .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                    return@setOnPreferenceClickListener false
                }
            }
            findPreference<Preference>("update_supervise")?.let {
                it.setOnPreferenceClickListener { preference ->
                    preference.summary = "업데이트중입니다..."
                    if (updateLocalSuperviseDatabase(requireContext())) {
                        preference.summary = "관리기관 목록을 업데이트하였습니다."
                    } else {
                        preference.summary = "다시 한 번 업데이트해주세요."
                    }
                    return@setOnPreferenceClickListener false
                }
            }
        }
    }
}
