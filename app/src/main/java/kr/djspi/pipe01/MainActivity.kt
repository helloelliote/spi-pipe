package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.room.Room
//import kotlinx.android.synthetic.main.activity_base.*
//import kotlinx.android.synthetic.main.activity_main.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.databinding.ActivityMainBinding
import kr.djspi.pipe01.nfc.StringParser.Companion.parseToJsonObject
import kr.djspi.pipe01.sql.SuperviseDatabase
import kr.djspi.pipe01.util.getOnlineServerData
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.updateLocalSuperviseDatabase
import java.io.Serializable

class MainActivity : LocationUpdate(), Serializable {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        Thread {
            checkPowerSaveMode()
            MerlinInstance.initiateNetworkMonitor(this)
        }.start()

        Thread {
            superviseDb = Room.databaseBuilder(
                this,
                SuperviseDatabase::class.java,
                "db_supervise"
            ).build()
            checkLocalSuperviseDatabase()
        }.start()

        mainBinding.layMainMenu1.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE
            if (!MerlinInstance.isConnected) {
                messageDialog(8)
                binding.progressbar.visibility = View.INVISIBLE
            } else if (currentLocation == null) {
//                startLocationUpdates()
                runLocationCounter(this@MainActivity)
            } else {
                locationFailureCount = 0
                startActivity(
                    Intent(this, NaverMapActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
        }

        mainBinding.layMainMenu2.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_spi_tag), Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }.show()
        }
        if (BuildConfig.BUILD_TYPE == "debug") {
            arrayOf(
                mainBinding.layMainDebug1,
                mainBinding.layMainDebug2,
                mainBinding.layMainDebug3,
                mainBinding.layMainDebug4
            ).forEach {
                it.visibility = View.VISIBLE
            }
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
        startLocationUpdates()
        if (binding.progressbar.visibility == View.VISIBLE) {
            binding.progressbar.visibility = View.GONE
        }
        if (!nfcUtil.isNfcEnabled()) {
            messageDialog(2, getString(R.string.popup_nfc_on), false)
            return
        }
        nfcUtil.onResume()
    }

    override fun onPause() {
        super.onPause()
        nfcUtil.onPause()
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
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun checkLocalSuperviseDatabase() {
        if (!AppPreference.defaultPrefs(this)["isSuperviseDbValid", false]!!) {
            updateLocalSuperviseDatabase(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            binding.progressbar.visibility = View.VISIBLE
            if (MerlinInstance.isConnected) {
                getOnlineServerData(it)
            } else {
                getOfflineTagData(it, 0, true)
            }
        }
    }

    fun getOfflineTagData(intent: Intent, index: Int, isOfflineMode: Boolean = true) {
        return try {
            val stringArrayList = nfcUtil.getRecord(intent)
            stringArrayList.removeAt(0)
            val data = parseToJsonObject(stringArrayList, index)
            startActivity(
                Intent(this, ViewActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("PipeView", data.toString())
            )
        } catch (e: Exception) {
            if (isOfflineMode) {
                messageDialog(4, getString(R.string.popup_error_offline_read_error), true)
            } else {
                messageDialog(3, getString(R.string.popup_error_not_spi), false)
            }
        }
    }
}

// TODO: Merlin Leak 연구, 지도보기 터치 후 무한 루프, 위치찾기 실패, 사진방향 가로눕기
