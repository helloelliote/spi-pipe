package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.nfc.StringParser.Companion.parseToJsonObject
import kr.djspi.pipe01.util.getOnlineServerData
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.updateLocalSuperviseDatabase
import org.jetbrains.anko.toast
import java.io.Serializable

class MainActivity : LocationUpdate(), Serializable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread(Runnable {
            checkPowerSaveMode()
            MerlinInstance.initiateNetworkMonitor(this)
            checkLocalSuperviseDatabase()
        }).start()
        setContentView(R.layout.activity_main)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        val layMain1 = findViewById<LinearLayout>(R.id.lay_main_menu1)
        layMain1?.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            if (!MerlinInstance.isConnected) {
                messageDialog(8)
                progressbar.visibility = View.INVISIBLE
            } else if (currentLocation == null) {
                toast(getString(R.string.toast_error_location))
            } else {
                startActivity(
                    Intent(this, NaverMapActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
        }
        val layMain2 = findViewById<LinearLayout>(R.id.lay_main_menu2)
        layMain2?.setOnClickListener {
            //            startActivity(Intent(applicationContext, UnityCallActivity::class.java))
            val intent = Intent(this, kr.djspi.unitysample.UnityPlayerActivity::class.java)
            intent.putExtra("arguments", 50)
            startActivity(intent)
//            Toast.makeText(this, getString(R.string.toast_spi_tag), Toast.LENGTH_SHORT).apply {
//                setGravity(Gravity.CENTER, 0, 0)
//            }.show()
        }
    }

    private fun checkPowerSaveMode() {
        runOnUiThread {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isPowerSaveMode) {
                messageDialog(9, getString(R.string.popup_power_save), false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MerlinInstance.registerNetworkCallback()
        if (progressbar.visibility == View.VISIBLE) {
            progressbar.visibility = View.GONE
        }
        if (!nfcUtil.isNfcEnabled()) {
            messageDialog(2, getString(R.string.popup_nfc_on), false)
            return
        }
        nfcUtil.onResume()
    }

    override fun onStart() {
        super.onStart()
        MerlinInstance.bind()
    }

    override fun onStop() {
        super.onStop()
        MerlinInstance.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressbar.visibility = View.INVISIBLE
    }

    private fun checkLocalSuperviseDatabase() {
        if (!AppPreference.defaultPrefs(this)["isSuperviseDbValid", false]!!) {
            updateLocalSuperviseDatabase(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            progressbar.visibility = View.VISIBLE
            when {
                MerlinInstance.isConnected -> getOnlineServerData(it)
                else -> getOfflineTagData(it, 0)
            }
        }
    }

    private fun getOfflineTagData(intent: Intent, index: Int) {
        try {
            val stringArrayList = nfcUtil.getRecord(intent)
            stringArrayList.removeAt(0)
            val data = parseToJsonObject(stringArrayList, index)
            startActivity(
                Intent(this, ViewActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("PipeView", data.toString())
            )
        } catch (e: Exception) {
            messageDialog(4, getString(R.string.popup_error_offline_read_error), true)
        }
    }
}
