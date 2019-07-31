package kr.djspi.pipe01.tab

import com.google.gson.JsonObject

interface OnRecordListener {
    val jsonObject: JsonObject

    val uri: String?

    fun onRecord(tag: String, result: Int)
}
