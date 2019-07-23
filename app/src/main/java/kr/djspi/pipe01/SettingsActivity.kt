package kr.djspi.pipe01

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.Const.*
import kr.djspi.pipe01.fragment.ListDialog
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.util.onNewIntentIgnore
import kr.djspi.pipe01.util.onPauseNfc
import kr.djspi.pipe01.util.onResumeNfc
import kr.djspi.pipe01.util.updateLocalSuperviseDatabase

class SettingsActivity : BaseActivity(), OnSelectListener {

    private var preferences: SharedPreferences = AppPreference.defaultPrefs(this)
    private var settingsFragment: SettingsFragment

    init {
        settingsFragment = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction().replace(R.id.settings, settingsFragment).commit()
        nmap_find.visibility = View.GONE
        setting_confirm.visibility = View.VISIBLE
        setting_confirm.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSelect(tag: String, index: Int, vararg text: String?) {
        if (index == -1) return
        when (tag) {
            TAG_PIPE -> {
                runOnUiThread {
                    settingsFragment.findPreference<Preference>("pipe_type")?.summary =
                        PIPE_TYPE_ENUMS[index].name
                }
                preferences["pipe_type"] = PIPE_TYPE_ENUMS[index].name
                preferences["pipe_type_id"] = index

            }
            TAG_SUPERVISE -> {
                text.let {
                    runOnUiThread {
                        settingsFragment.findPreference<Preference>("supervise")?.summary =
                            text[0]
                    }
                    preferences["supervise"] = text[0]
                }
            }
        }
    }

    private fun restoreInstanceState() {
        runOnUiThread {
            settingsFragment.findPreference<Preference>("pipe_type")?.summary =
                preferences["pipe_type", ""]
            settingsFragment.findPreference<Preference>("supervise")?.summary =
                preferences["supervise", ""]
        }
    }

    override fun onResume() {
        super.onResume()
        restoreInstanceState()
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

    private inner class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey)
            findPreference<Preference>("help")?.let {
                it.setOnPreferenceClickListener {
                    AlertDialog.Builder(context!!)
                        .setIcon(R.drawable.ic_help)
                        .setTitle("입력내용 일괄적용")
                        .setMessage(getString(R.string.message_preset_help))
                        .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                    return@setOnPreferenceClickListener false
                }
            }
            findPreference<Preference>("pipe_type")?.let {
                it.setOnPreferenceClickListener {
                    ListDialog().show(childFragmentManager, TAG_PIPE)
                    return@setOnPreferenceClickListener false
                }
            }
            findPreference<Preference>("supervise")?.let {
                it.setOnPreferenceClickListener {
                    ListDialog().show(childFragmentManager, TAG_SUPERVISE)
                    return@setOnPreferenceClickListener false
                }
            }
            findPreference<Preference>("update_supervise")?.let {
                it.setOnPreferenceClickListener { preference ->
                    updateLocalSuperviseDatabase(context!!)
                    preference.summary = "관리기관 목록을 업데이트하였습니다."
                    return@setOnPreferenceClickListener false
                }
            }
        }
    }
}
