package kr.djspi.pipe01

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.system.exitProcess

class Intro : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private var listener: InstallStateUpdatedListener? = null

    /**
     * (isDelayed) 지정 시간 후 전환되는 스플래시 화면
     * (!isDelayed) 앱 로딩이 끝나면 바로 전환되는 스플래시 화면
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SPI", "IntroActivity\nonCreate()")

        setContentView(R.layout.activity_intro)

        if (BuildConfig.BUILD_TYPE == "release") {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageName)
            }
            Log.d("SPI", "IntroActivity\nPackageName: $installer")
            if (installer != "com.android.vending") {
                finishAffinity()
                System.runFinalization()
                exitProcess(0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("SPI", "IntroActivity\nonResume()")
        // ref: 인앱 업데이트 지원 (https://developer.android.com/guide/playcore/in-app-updates/kotlin-java?hl=ko)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SPI", "IntroActivity\nonDestroy()")
        // When status updates are no longer needed, unregister the listener.
        if (listener != null) {
            Log.d("SPI", "IntroActivity\nonDestroy(): listener unregister")
            appUpdateManager.unregisterListener(listener!!)
        }
    }

    private fun runMainActivity() {
        Log.d("SPI", "IntroActivity\nrunMainActivity()")
        Handler(
            Looper.getMainLooper()
        ).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 750)
    }

    private fun checkUpdate() {
        Log.d("SPI", "IntroActivity\ncheckUpdate()")
        // Checks that the platform will allow the specified type of update.
        try {
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

                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d("SPI", "IntroActivity\nonResume: Downloaded")
                    popupToastForCompleteUpdate()
                }
            }
        } catch (e: Exception) {
            runMainActivity()
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            if (result.resultCode != RESULT_OK) {
                Log.d("SPI", "IntroActivity\nResult cancel or fail")
                Toast.makeText(this, "플레이스토어에서 앱을 업데이트해주세요.", Toast.LENGTH_LONG).show()
                // If the update is canceled or fails, you can request to start the update again.
                runMainActivity()
            }
        }

    private fun AppUpdateInfo.flexibleUpdate() {
        Log.d("SPI", "IntroActivity\ncheckUpdate: Flexible update")
        // Create a listener to track request state updates.
        listener = InstallStateUpdatedListener { state ->
            // (Optional) Provide a download progress bar.
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytes = state.bytesDownloaded()
                    val totalBytes = state.totalBytesToDownload()
                    val bytesToMegaBytes = ceil((bytes / 1e6) * 10.0).toLong() / 10.0
                    val totalBytesToMegabytes = ceil((totalBytes / 1e6) * 10.0).toLong() / 10.0
                    // Show update progress bar.
                    val percentage = bytes * 100 / totalBytes
                    val progress = "다운로드 중... $percentage% (${bytesToMegaBytes}MB / ${totalBytesToMegabytes}MB)"
                    findViewById<TextView>(R.id.update_text).text = progress
                    findViewById<LinearLayout>(R.id.lay_update).visibility = View.VISIBLE
                }

                InstallStatus.DOWNLOADED -> {
                    popupSnackbarForCompleteUpdate()
                }

                else -> {}
            }

        }
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

        // Create a listener to track request state updates.
        listener = InstallStateUpdatedListener { state ->
            // (Optional) Provide a download progress bar.
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val megabytesDownloaded =
                        ((state.bytesDownloaded() / 1e6) * 10.0).roundToLong() / 10.0
                    val totalMegabytesToDownload =
                        ((state.totalBytesToDownload() / 1e6) * 10.0).roundToLong() / 10.0
                    val percentage =
                        (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toString()
                    val progress =
                        "업데이트 중입니다...\n$megabytesDownloaded / $totalMegabytesToDownload ($percentage %)"
                    findViewById<TextView>(R.id.update_text).text = progress
                    Log.d("SPI", "IntroActivity\nmegabytesDownloaded: $megabytesDownloaded")
                    Log.d("SPI", "IntroActivity\ntotalMegabytesToDownload: $totalMegabytesToDownload")
                    Log.d("SPI", "IntroActivity\npercentage: $percentage")
                    Log.d("SPI", "IntroActivity\nprogress: $progress")
                }

                InstallStatus.DOWNLOADED -> {
                    popupToastForCompleteUpdate()
                }

                else -> {}
            }
            // Log state or install the update.
        }

        // Request the update.
        appUpdateManager.registerListener(listener!!)
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            this,
            // an activity result launcher registered via registerForActivityResult
            activityResultLauncher,
            // immediate update
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    private fun popupSnackbarForCompleteUpdate() {
        Log.d("SPI", "IntroActivity\nCompleteUpdate: 업데이트 설치")
        Snackbar.make(
            findViewById(R.id.update_text), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("재실행") { appUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.material_dynamic_primary70))
            show()
        }
    }

    private fun popupToastForCompleteUpdate() {
        Log.d("SPI", "IntroActivity\nCompleteUpdate: 업데이트 설치")
        Toast.makeText(this, "업데이트를 설치합니다.", Toast.LENGTH_LONG).show()
        appUpdateManager.completeUpdate()
    }
}
