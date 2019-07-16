package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class Intro : AppCompatActivity() {

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

        Handler().postDelayed({
            getSharedPreferences(packageName, Context.MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }, 750)
    }
}