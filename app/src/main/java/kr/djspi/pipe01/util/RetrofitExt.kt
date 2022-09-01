package kr.djspi.pipe01.util

import android.content.Context
import android.content.Intent
import com.google.gson.JsonObject
import kr.djspi.pipe01.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.BaseActivity.Companion.superviseDb
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.sql.Supervise
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

private const val HOST_SPI = "espi.kr"

fun updateLocalSuperviseDatabase(context: Context): Boolean {
    Retrofit2x.getSuperviseDatabase().enqueue(object : RetrofitCallback() {
        override fun onResponse(response: JsonObject) {
            Thread {
                val superviseDao = superviseDb!!.dao()
                superviseDb!!.dao().all
                if (superviseDb?.isOpen!!) {
                    val jsonArray = response["data"].asJsonArray
                    jsonArray.forEach { element ->
                        val obj = element.asJsonObject
                        superviseDao.insert(Supervise(obj["id"].asInt, obj["supervise"].asString))
                    }
                    AppPreference.defaultPrefs(context)["isSuperviseDbValid"] = true
                } else {
                    AppPreference.defaultPrefs(context)["isSuperviseDbValid"] = false
                }
            }.start()
        }
    })
    return AppPreference.defaultPrefs(context)["isSuperviseDbValid"]!!
}

private fun isServerReachable(): Boolean {
    return try {
        Socket().use {
            // Port =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
            it.connect(InetSocketAddress(HOST_SPI, 80), 2000)
        }
        true
    } catch (e: IOException) {
        false
    }
}

fun MainActivity.getOnlineServerData(intent: Intent) {
    Thread({
        if (!isServerReachable()) {
            getOfflineTagData(intent, 0, false)
        } else {
            val tag = nfcUtil.onNewTagIntent(intent)
            val serial = bytesToHex(tag.id)
            val jsonQuery = JsonObject()
            jsonQuery.addProperty("spi_serial", serial)
            Retrofit2x.getSpi("spi-get", jsonQuery).enqueue(object : RetrofitCallback() {
                override fun onResponse(response: JsonObject) {
                    if (response["total_count"].asInt >= 1) {
                        processServerData(response, jsonQuery, serial)
                    } else {
                        getOfflineTagData(intent, 0, false)
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    messageDialog(8, throwable.message)
                    throwable.printStackTrace()
                }
            })
        }
    }).start()
}

fun MainActivity.processServerData(response: JsonObject, jsonQuery: JsonObject, serial: String) {
    val jsonArray = response["data"].asJsonArray
    val jsonObject = jsonArray[0].asJsonObject
    if (jsonObject["pipe_count"].asInt == 0) {
        try {
            this.startActivity(
                Intent(this.applicationContext, RegisterActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("RegisterActivity", parseServerData(jsonObject, serial))
            )
        } catch (e: UnsupportedOperationException) {
            messageDialog(11, getString(R.string.popup_error_not_spi_renewal))
            return
        }
    } else {
        if (jsonObject.get("spi_count").isJsonNull) {
            messageDialog(3, getString(R.string.popup_error_not_spi), false)
        } else {
            Thread({
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
