package kr.djspi.pipe01.tab

import android.net.Uri
import com.google.gson.JsonObject

interface OnRecordListener {
    val jsonObject: JsonObject

    val uri: Uri?

    fun onRecord(tag: String, result: Int)
}
