package kr.djspi.pipe01.util

import android.content.Context
import android.content.Intent
import com.google.gson.JsonObject
import kr.djspi.pipe01.*
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.BaseActivity.Companion.superviseDb
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.sql.Supervise
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun updateLocalSuperviseDatabase(context: Context) {
    Retrofit2x.getSuperviseDatabase().enqueue(object : RetrofitCallback() {
        override fun onResponse(response: JsonObject) {
            val jsonArray = response["data"].asJsonArray
            val superviseDao = superviseDb?.dao()
            jsonArray.forEach { element ->
                val obj = element.asJsonObject
                Thread(Runnable {
                    superviseDao?.insert(Supervise(obj["id"].asInt, obj["supervise"].asString))
                }).start()
            }
            AppPreference.defaultPrefs(context)["isSuperviseDbValid"] = true
        }
    })
}

fun MainActivity.getOnlineServerData(intent: Intent) {
    Thread(Runnable {
        val tag = nfcUtil.onNewTagIntent(intent)
//        if (tag == null)
        val serial = bytesToHex(tag.id)
        val jsonQuery = JsonObject()
        jsonQuery.addProperty("spi_serial", serial)
        Retrofit2x.getSpi("spi-get", jsonQuery).enqueue(object : RetrofitCallback() {
            override fun onResponse(response: JsonObject) {
                if (response["total_count"].asInt >= 1) {
                    processServerData(response, jsonQuery, serial)
                } else
                    messageDialog(3, getString(R.string.popup_error_not_spi), false)
            }

            override fun onFailure(throwable: Throwable) {
                messageDialog(8, throwable.message)
                throwable.printStackTrace()
            }
        })
    }).start()
}

fun MainActivity.processServerData(response: JsonObject, jsonQuery: JsonObject, serial: String) {
    val jsonArray = response["data"].asJsonArray
    val jsonObject = jsonArray[0].asJsonObject
    if (jsonObject["pipe_count"].asInt == 0) {
        startActivity(
            Intent(applicationContext, RegisterActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(
                    "RegisterActivity",
                    parseServerData(jsonObject, serial)
                )
        )
    } else {
        Thread(Runnable {
            Retrofit2x.getSpi("pipe-get", jsonQuery).enqueue(object : RetrofitCallback() {
                override fun onResponse(response: JsonObject) {
                    val elements = response["data"].asJsonArray
                    startActivity(
                        Intent(applicationContext, ViewActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("PipeView", elements[0].toString())
                    )
                }

                override fun onFailure(throwable: Throwable) {
                    messageDialog(8, throwable.message)
                }
            })
        }).start()
    }
}

fun bytesToHex(bytes: ByteArray): String {
    val builder: StringBuilder = StringBuilder()
    bytes.forEach {
        val hex = String.format("%02X", it)
        builder.append(hex)
    }
    val string = builder.toString().replace("(..)".toRegex(), "$1:")
    return string.take(string.length - 1)
}

/**
 * 직접 #onResponse 또는 #onFailure 를 호출하는 대신 인터페이스 메서드를 호출해 코드 중복을 피한다.
 */
open class RetrofitCallback : Callback<JsonObject>, OnRetrofitListener {

    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>?) {
        response?.let {
            if (it.isSuccessful) {
                onResponse(it.body()!!)
            } else onFailure(call, Throwable(it.message()))
        }
    }

    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
        onFailure(t)
    }

    override fun onResponse(response: JsonObject) {
    }

    override fun onFailure(throwable: Throwable) {
    }
}

private interface OnRetrofitListener {

    fun onResponse(response: JsonObject)

    fun onFailure(throwable: Throwable)
}
