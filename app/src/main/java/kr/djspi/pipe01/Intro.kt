package kr.djspi.pipe01

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
//import kotlinx.android.synthetic.main.activity_intro.*
import kr.djspi.pipe01.databinding.ActivityIntroBinding
import kotlin.math.roundToLong
import kotlin.system.exitProcess

class Intro : AppCompatActivity() {

    private lateinit var introBinding: ActivityIntroBinding
    private lateinit var appUpdateManager: AppUpdateManager
    private var listener: InstallStateUpdatedListener? = null

    /**
     * (isDelayed) 지정 시간 후 전환되는 스플래시 화면
     * (!isDelayed) 앱 로딩이 끝나면 바로 전환되는 스플래시 화면
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SPI", "IntroActivity\nonCreate() start")

        introBinding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(introBinding.root)

        if (BuildConfig.BUILD_TYPE == "release") {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageName)
            }
            Log.d("SPI", "IntroActivity\nPackageName: $installer")
            if (!installer!!.startsWith("com.android.vending")) {
                finishAffinity()
                System.runFinalization()
                exitProcess(0)
            }
        }

        // ref: 인앱 업데이트 지원 (https://developer.android.com/guide/playcore/in-app-updates/kotlin-java?hl=ko)
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Create a listener to track request state updates.
        listener = InstallStateUpdatedListener { state ->
            // (Optional) Provide a download progress bar.
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                val megabytesDownloaded =
                    ((state.bytesDownloaded() / 1e6) * 10.0).roundToLong() / 10.0
                val totalMegabytesToDownload =
                    ((state.totalBytesToDownload() / 1e6) * 10.0).roundToLong() / 10.0
                val percentage =
                    (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toString()
                val progress =
                    "업데이트 중입니다...\n$megabytesDownloaded / $totalMegabytesToDownload ($percentage %)"
                introBinding.updateText.text = progress
                Log.d("SPI", "IntroActivity\nmegabytesDownloaded: $megabytesDownloaded")
                Log.d("SPI", "IntroActivity\ntotalMegabytesToDownload: $totalMegabytesToDownload")
                Log.d("SPI", "IntroActivity\npercentage: $percentage")
                Log.d("SPI", "IntroActivity\nprogress: $progress")
            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupToastForCompleteUpdate()
            }
            // Log state or install the update.
        }

        checkUpdate()
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                Log.d("SPI", "IntroActivity\nonResume: Developer triggered update in progress")
                appUpdateInfo.immediateUpdate()
            }
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d("SPI", "IntroActivity\nonResume: Downloaded")
                popupToastForCompleteUpdate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SPI", "IntroActivity\nonDestroy() start")
        // When status updates are no longer needed, unregister the listener.
        if (listener != null) {
            Log.d("SPI", "IntroActivity\nonDestroy(): listener unregister")
            appUpdateManager.unregisterListener(listener!!)
        }
    }

    private fun runMainActivity() {
        Log.d("SPI", "IntroActivity\nrunMainActivity() start")
        Handler().postDelayed({
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }, 750)
    }

    private fun checkUpdate() {
        Log.d("SPI", "IntroActivity\ncheckUpdate() start")
        // Checks that the platform will allow the specified type of update.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Log.d("SPI", "IntroActivity\nappUpdateInfoTask success listener")
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d("SPI", "IntroActivity\ncheckUpdate: Update available")
                    when {
                        // This example applies an immediate update.
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                            appUpdateInfo.immediateUpdate()
                        }
                        // To apply a flexible update instead, pass in AppUpdateType.FLEXIBLE
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                            appUpdateInfo.flexibleUpdate()
                        }
                    }
                }

                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // If an in-app update is already running, resume the update.
                    appUpdateInfo.immediateUpdate()
                }

                else -> {
                    Log.d("SPI", "IntroActivity\nUpdate not available")
                    runMainActivity()
                }
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            when (result.resultCode) {
                RESULT_OK -> runMainActivity()
                // If the update is canceled or fails, you can request to start the update again.
                else -> {
                    Log.d("SPI", "IntroActivity\nResult cancel or fail")
                    Toast.makeText(this, "플레이스토어에서 앱을 업데이트해주세요.", Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun AppUpdateInfo.flexibleUpdate() {
        Log.d("SPI", "IntroActivity\ncheckUpdate: Flexible update")
        appUpdateManager.registerListener(listener!!)
        appUpdateManager.startUpdateFlowForResult(
            this,
            activityResultLauncher,
            // flexible updates
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        )
    }

    private fun AppUpdateInfo.immediateUpdate() {
        Log.d("SPI", "IntroActivity\ncheckUpdate: Immediate update")
        // Request the update.
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            this,
            // an activity result launcher registered via registerForActivityResult
            activityResultLauncher,
            // immediate update
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    private fun popupToastForCompleteUpdate() {
        Log.d("SPI", "IntroActivity\nCompleteUpdate: 업데이트 설치")
        Toast.makeText(this, "업데이트를 설치합니다.", Toast.LENGTH_LONG).show()
        appUpdateManager.completeUpdate()
    }
}
