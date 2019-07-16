package kr.djspi.pipe01

import android.app.Activity
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import kr.djspi.pipe01.fragment.MessageDialog

fun Activity.settingsMenuEnabled(): Boolean {
    return when (this) {
        is ViewActivity, is RegisterActivity, is SpiLocationActivity, is SpiPostActivity -> false
        else -> true
    }
}

fun AppCompatActivity.messageDialog(issue: Int, tag: String = "", cancelable: Boolean = true) {
    MessageDialog.newInstance(issue, cancelable).show(supportFragmentManager, tag)
}

fun Activity.screenScale(): Float {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels / 1440.0f
}