package kr.djspi.pipe01

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.NaverMapSdk
import kr.djspi.pipe01.databinding.ActivityBaseBinding
//import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.sql.SuperviseDatabase
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.screenScale

open class BaseActivity : AppCompatActivity() {

    lateinit var binding: ActivityBaseBinding
    lateinit var nfcUtil: NfcUtil
    var locationFailureCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcUtil = NfcUtil(this, javaClass)
        defPackage = this@BaseActivity.packageName
        if (screenRatio == 0.0f) {
            screenRatio = this@BaseActivity.screenScale()
        }
    }

    fun runLocationCounter(context: Context) {
        when {
            locationFailureCount == 1 -> {
                NaverMapSdk.getInstance(context).flushCache {}
            }

            locationFailureCount >= 2 -> {
                locationFailureCount++
                binding.progressbar.visibility = View.INVISIBLE
                messageDialog(10, getString(R.string.popup_fail_location), false)
                return
            }
        }
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                locationFailureCount++
                binding.progressbar.visibility = View.INVISIBLE
                messageDialog(0, getString(R.string.popup_error_location))
            }
        }.start()
    }

    companion object {
        lateinit var defPackage: String
        lateinit var currentSerial: String
        var currentLocation: Location? = null
        var superviseDb: SuperviseDatabase? = null
        var screenRatio: Float = 0.0f
        var isReadyForPost: Boolean =
            false // (SpiPostActivity.class) 원치 않은 시점에서 태깅 동작이 발생하지 않도록 한다.
    }
}
