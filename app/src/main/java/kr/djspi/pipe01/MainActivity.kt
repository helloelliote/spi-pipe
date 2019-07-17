package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.novoda.merlin.*
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_main.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.nfc.NfcUtil.getRecord
import kr.djspi.pipe01.nfc.NfcUtil.isNfcEnabled
import kr.djspi.pipe01.nfc.StringParser.parseToJsonObject
import kr.djspi.pipe01.util.getOnlineServerData
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.updateLocalSuperviseDatabase
import org.jetbrains.anko.toast
import java.io.Serializable

class MainActivity : LocationUpdate(), Serializable, Connectable, Disconnectable, Bindable {

    private lateinit var merlin: Merlin
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPowerSaveMode()
        Thread(Runnable {
            setNetworkCallback()
            checkLocalSuperviseDatabase()
        }).start()
        setContentView(R.layout.activity_main)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        lay_main1.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            if (!isConnected) {
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
        lay_main2.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_spi_tag), Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }.show()
        }
    }

    private fun setNetworkCallback() {
        merlin = Merlin.Builder()
            .withConnectableCallbacks()
            .withDisconnectableCallbacks()
            .withBindableCallbacks()
            .build(this)
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
        registerNetworkCallback()
        if (progressbar.visibility == View.VISIBLE) {
            progressbar.visibility = View.GONE
        }
        if (!isNfcEnabled()) {
            messageDialog(2, getString(R.string.popup_nfc_on), false)
            return
        }
        nfcUtil.onResume()
    }

    private fun registerNetworkCallback() {
        merlin.registerConnectable(this)
        merlin.registerDisconnectable(this)
        merlin.registerBindable(this)
    }

    override fun onConnect() {
        isConnected = true
    }

    override fun onDisconnect() {
        isConnected = false
    }

    override fun onBind(networkStatus: NetworkStatus) {
        if (!networkStatus.isAvailable) {
            onDisconnect()
        }
    }

    override fun onStart() {
        super.onStart()
        merlin.bind()
    }

    override fun onStop() {
        super.onStop()
        merlin.unbind()
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
        when (intent) {
            null -> return
            else -> {
                progressbar.visibility = View.VISIBLE
                when {
                    isConnected -> getOnlineServerData(intent)
                    else -> getOfflineTagData(intent, 0)
                }
            }
        }
    }

    private fun getOfflineTagData(intent: Intent, index: Int) {
        try {
            val stringArrayList = getRecord(intent)
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