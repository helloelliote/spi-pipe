package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.novoda.merlin.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_spi_post.*
import org.jetbrains.anko.toast
import java.io.Serializable

class MainActivity : LocationUpdate(), Serializable, Connectable, Disconnectable, Bindable {

    private lateinit var merlin: Merlin
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread(Runnable {
            checkLocalSuperviseDatabase()
            setNetworkCallback()
        }).start()
        checkPowerSaveMode()
        setContentView(R.layout.activity_main)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        lay_main1.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (!isConnected) {
                messageDialog(8)
                progressBar.visibility = View.INVISIBLE
            } else if (currentLocation == null) {
                toast(getString(R.string.toast_error_location))
            } else {
                startActivity(Intent(this, NaverMapActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
        lay_main2.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_spi_tag), Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0);
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

    private fun checkLocalSuperviseDatabase() {

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
}