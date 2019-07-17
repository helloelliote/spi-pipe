package kr.djspi.pipe01.util

import android.content.Context
import android.content.Intent
import com.google.gson.JsonObject
import kr.djspi.pipe01.*
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.BaseActivity.Companion.superviseDb
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.sql.Supervise
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val retrofit2x = Retrofit2x()

fun updateLocalSuperviseDatabase(context: Context) {
    val jsonQuery = JsonObject()
    jsonQuery.addProperty("request", "supervise-get")
    retrofit2x.getSuperviseDatabase(jsonQuery.toString()).enqueue(object : RetrofitCallback() {
        override fun onResponses(response: JsonObject?) {
            response?.let {
                val jsonArray = it["data"].asJsonArray
                val superviseDao = superviseDb?.dao()
                jsonArray.forEach { element ->
                    val obj = element.asJsonObject
                    superviseDao?.insert(
                        Supervise(obj["id"].asInt, obj["supervise"].asString)
                    )
                }
                AppPreference.defaultPrefs(context)["isSuperviseDbValid"] = true
            }
        }
    })
}

fun MainActivity.getOnlineServerData(intent: Intent) {
    val tag = NfcUtil.onNewTagIntent(intent)
    val serial = NfcUtil.bytesToHex(tag.id)
    val jsonQuery = JsonObject()
    jsonQuery.addProperty("spi_serial", serial)
    retrofit2x.getServerData(jsonQuery.toString()).enqueue(object : RetrofitCallback() {
        override fun onResponses(response: JsonObject?) {
            response?.let {
                if (it["total_count"].asInt >= 1) {
                    processServerData(response = it, query = jsonQuery, serial = serial)
                } else {
                    messageDialog(3, getString(R.string.popup_error_not_spi), false)
                }
            }
        }

        override fun onFailures(throwable: Throwable) {
            messageDialog(8, throwable.message ?: "")
            throwable.printStackTrace()
        }
    })
}

fun MainActivity.processServerData(response: JsonObject, query: JsonObject, serial: String) {
    val jsonObject = response["data"].asJsonObject
    if (jsonObject["pipe_count"].asInt == 0) {
        startActivity(
            Intent(applicationContext, RegisterActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(
                    "RegisterActivity",
                    parseServerData(response, serial)
                )
        )
    } else {
        retrofit2x.getSpi(query.toString()).enqueue(object : RetrofitCallback() {
            override fun onResponses(response: JsonObject?) {
                response?.let {
                    val elements = it["data"].asJsonArray
                    startActivity(
                        Intent(applicationContext, ViewActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("PipeView", elements[0].toString())
                    )
                }
            }

            override fun onFailures(throwable: Throwable) {
                messageDialog(8, throwable.message ?: "")
            }
        })
    }
}

/**
 * 직접 #onResponse 또는 #onFailure 를 호출하는 대신 인터페이스 메서드를 호출해 코드 중복을 피한다.
 */
open class RetrofitCallback : Callback<JsonObject>,
    OnRetrofitListener {

    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
        if (response.isSuccessful) {
            onResponses(response.body())
        } else onFailure(call, Throwable(response.message()))
    }

    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
        onFailures(t)
    }

    override fun onResponses(response: JsonObject?) {

    }

    override fun onFailures(throwable: Throwable) {

    }
}

internal interface OnRetrofitListener {
    fun onResponses(response: JsonObject?)

    fun onFailures(throwable: Throwable)
}
