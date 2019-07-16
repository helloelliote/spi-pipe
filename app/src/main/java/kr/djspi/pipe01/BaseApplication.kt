package kr.djspi.pipe01

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (USE_LEAK_CANARY) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)
        }
    }

    companion object {
        private const val USE_LEAK_CANARY = false
    }
}