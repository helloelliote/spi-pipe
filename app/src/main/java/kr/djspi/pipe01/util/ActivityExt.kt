package kr.djspi.pipe01.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.text.Spanned
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.fromHtml
import kr.djspi.pipe01.RegisterActivity
import kr.djspi.pipe01.SpiLocationActivity
import kr.djspi.pipe01.SpiPostActivity
import kr.djspi.pipe01.ViewActivity
import kr.djspi.pipe01.fragment.MessageDialog

fun Activity.settingsMenuEnabled(): Boolean {
    return when (this) {
        is ViewActivity, is RegisterActivity, is SpiLocationActivity, is SpiPostActivity -> false
        else -> true
    }
}

fun AppCompatActivity.messageDialog(issue: Int, tag: String? = "", cancelable: Boolean = true) {
    try {
        MessageDialog.getInstance(issue, cancelable).show(supportFragmentManager, tag)
    } catch (ignore: Exception) {
      
    }
}

fun Activity.screenScale(): Float {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels / 1440.0f
}

fun Activity.screenSize(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels
    return if (height > width) height else width
}

fun Activity.onResumeNfc() {
    val pendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
    )
    NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, null, null)
}

fun Activity.onPauseNfc() {
    val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    nfcAdapter.disableForegroundDispatch(this)
}

fun Activity.onNewIntentIgnore() {
    if (NfcAdapter.ACTION_TAG_DISCOVERED == intent?.action) {
        // drop NFC events
    }
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun fromHtml(source: String): Spanned {
    return fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY)
}
