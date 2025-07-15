package kr.djspi.pipe01

import android.content.Intent
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.djspi.pipe01.databinding.ActivitySpiPostBinding
import kr.djspi.pipe01.dto.Entry.Companion.parseEntry
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.network.ProgressBody
import kr.djspi.pipe01.network.ProgressBody.UploadCallback
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.nfc.StringParser.Companion.parseToStringArray
import kr.djspi.pipe01.util.*
import kr.djspi.pipe01.util.applySystemBarInsets
import okhttp3.MultipartBody
import java.io.File
import java.io.Serializable

class SpiPostActivity : BaseActivity(), UploadCallback, Serializable {

    private lateinit var postBinding: ActivitySpiPostBinding
    private lateinit var entries: ArrayList<*>
    private lateinit var jsonObject: JsonObject
    private lateinit var progressDrawable: Drawable
    private var file: File? = null
    private var uri: String? = null
    private var part: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postBinding = ActivitySpiPostBinding.inflate(layoutInflater)
        setContentView(postBinding.root)

        setNavigationDrawer(
            postBinding.layAppbar.toolbar,
            postBinding.navView.navView,
            postBinding.drawerLayout
        )

        isReadyForPost = false
        entries = intent.getSerializableExtra("entry") as ArrayList<*>
        jsonObject = parseEntry(entries, 0, "", "")
        intent.getSerializableExtra("Photos")?.let {
            if (it is SpiPhotoObject) {
                file = it.file
                uri = it.uri
                part = getMultipart(file!!, "image")
            }
        }

        postBinding.layAppbar.nmapFind.setOnClickListener {
            when {
                currentLocation != null -> {
                    locationFailureCount = 0
                    startActivity(
                        Intent(this, NaverMapActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                }

                else -> {
                    runLocationCounter(this)
                }
            }
        }

        postBinding.navView.navClose.setOnClickListener { postBinding.drawerLayout.close() }

        postBinding.txtWrite.text = fromHtml(getString(R.string.write_instruction))
        progressDrawable = ((postBinding.progressBar.progressDrawable) as LayerDrawable).getDrawable(1)
        progressDrawable.setTint(YELLOW)

        runOnUiThread {
            messageDialog(5, getString(R.string.popup_read_only), false)
        }
    }

    private fun getMultipart(file: File, fileType: String): MultipartBody.Part? {
        try {
            part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                ProgressBody(file, fileType, this)
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
        return part
    }

    override fun onNewIntent(intent: Intent?) {
        if (!isReadyForPost) {
            onNewIntentIgnore()
            return
        }
        super.onNewIntent(intent)
        if (intent == null) return
        if (processTag(intent, jsonObject, 0)) {
            setSpiAndPipe()
        }
    }

    /**
     * 태그에 쓰기 작업을 지시, 결과를 출력
     * (성공) MainActivity 로 돌아감 + 성공 메시지 표시
     * (실패) 재시도 요청 토스트 메시지 표시
     *
     * @param intent 전달된 태그 인텐트
     * @see NfcUtil.writeTag
     */
    private fun processTag(intent: Intent, json: JsonObject, index: Int): Boolean {
        if (nfcUtil.getSerial(intent) != currentSerial) {
            messageDialog(12, getString(R.string.popup_error_serial_mismatch), false)
            return false
        }
        val strings = parseToStringArray(json, index)
        return if (nfcUtil.writeTag(intent, strings)) {
            true
        } else {
            messageDialog(0, getString(R.string.popup_write_retry), false)
            false
        }
    }

    private fun setSpiAndPipe() {
        postBinding.progressBarText.visibility = View.VISIBLE
        onInitiate(0)
        Retrofit2x.postSpi(Gson().toJson(entries), part).enqueue(object : RetrofitCallback() {
            override fun onResponse(response: JsonObject) {
                onFinish(100)
                postBinding.progressBarText.visibility = View.INVISIBLE
                messageDialog(6, getString(R.string.popup_write_success), false)
                file?.let {
                    if (it.exists()) it.delete()
                    uri = null
                }
            }

            override fun onFailure(throwable: Throwable) {
                onError()
                postBinding.progressBarText.visibility = View.INVISIBLE
                messageDialog(7, throwable.message, false)
                throwable.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        postBinding.drawerLayout.applySystemBarInsets(
            postBinding.navView.navView.getHeaderView(0),
            postBinding.layAppbar.root
        )

        nfcUtil.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        onInitiate(0)
    }

    override fun onBackPressed() {
        if (postBinding.drawerLayout.isOpen) postBinding.drawerLayout.close()
        else super.onBackPressed()
    }

    override fun onInitiate(percentage: Int) {
        postBinding.progressBar.progress = percentage
        progressDrawable.setTint(YELLOW)
    }

    override fun onProgress(percentage: Int) {
        postBinding.progressBar.progress = percentage
    }

    override fun onError() {
        progressDrawable.setTint(RED)
    }

    override fun onFinish(percentage: Int) {
        postBinding.progressBar.progress = percentage
        progressDrawable.setTint(GREEN)
    }
}
