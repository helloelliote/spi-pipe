package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_intro.*
import kr.djspi.pipe01.Const.REQUEST_APP_UPDATE
import java.lang.Math.round
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

        if (BuildConfig.BUILD_TYPE == "release") {
            val installer = packageManager.getInstallerPackageName(packageName)
            if (!installer!!.startsWith("com.android.vending")) {
                finishAffinity()
                System.runFinalization()
                exitProcess(0)
            }
        }

        setContentView(R.layout.activity_intro)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
    }

    private fun checkUpdate() {
        try {
            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    runUpdate(appUpdateInfo)
                } else {
                    runMainActivity()
                }
            }
        } catch (e: Exception) {
            runMainActivity()
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun runUpdate(appUpdateInfo: AppUpdateInfo) {
        // Create a listener to track request state updates.
        listener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val megabytesDownloaded =
                        round((state.bytesDownloaded() / 1e6) * 10.0) / 10.0
                    val totalMegabytesToDownload =
                        round((state.totalBytesToDownload() / 1e6) * 10.0) / 10.0
                    val percentage =
                        (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toString()
                    val progress =
                        "업데이트 중입니다...\n$megabytesDownloaded / $totalMegabytesToDownload ($percentage %)"
                    update_text.text = progress
                }
                InstallStatus.DOWNLOADED -> {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    Toast.makeText(this, "업데이트를 설치합니다.", Toast.LENGTH_LONG).show()
                    appUpdateManager.completeUpdate()
                }
            }
            // Log state or install the update.
        }
        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener!!)
        // Request the update.
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            AppUpdateType.IMMEDIATE,
            // The current activity making the update request.
            this,
            // Include a request code to later monitor this update request.
            REQUEST_APP_UPDATE
        )
    }

    private fun runMainActivity() {
        Handler().postDelayed({
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }, 750)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "플레이스토어에서 앱을 업데이트해주세요.", Toast.LENGTH_LONG).show()
                runMainActivity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // When status updates are no longer needed, unregister the listener.
        if (listener != null) {
            appUpdateManager.unregisterListener(listener!!)
        }
    }
}
