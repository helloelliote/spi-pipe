package kr.djspi.pipe01

import android.content.Intent
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.Serializable
import kotlinx.android.synthetic.main.activity_spi_post.*
import kr.djspi.pipe01.dto.Entry.Companion.parseEntry
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.network.ProgressBody
import kr.djspi.pipe01.network.ProgressBody.UploadCallback
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.nfc.StringParser.Companion.parseToStringArray
import kr.djspi.pipe01.util.RetrofitCallback
import kr.djspi.pipe01.util.fromHtml
import kr.djspi.pipe01.util.messageDialog
import okhttp3.MultipartBody

class SpiPostActivity : BaseActivity(), UploadCallback, Serializable {

    private lateinit var entries: ArrayList<*>
    private lateinit var jsonObject: JsonObject
    private lateinit var progressBar: ProgressBar
    private lateinit var progressDrawable: Drawable
    private var file: File? = null
    private var part: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entries = intent.getSerializableExtra("entry") as ArrayList<*>
        jsonObject = parseEntry(entries, 0, "", "")
        intent.getSerializableExtra("SpiPhotoObject")?.let {
            if (it is SpiPhotoObject) {
                file = it.file
                part = getMultipart(file!!, "image")
            }
        }
        setContentView(R.layout.activity_spi_post)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        txt_write.text = fromHtml(getString(R.string.write_instruction))
        progressBar = findViewById(R.id.progressBar)
        progressDrawable = ((progressBar.progressDrawable) as LayerDrawable).getDrawable(1)
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
        super.onNewIntent(intent)
        if (intent == null) return
        if (processTag(intent, jsonObject, 0)) {
            setSpiAndPipe()
        } else messageDialog(0, getString(R.string.popup_write_retry), false)
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
        var isWriteSuccess = false
        val strings = parseToStringArray(json, index)
        if (nfcUtil.writeTag(intent, strings)) {
            isWriteSuccess = true
        }
        return isWriteSuccess
    }

    private fun setSpiAndPipe() {
        progressBar_text.visibility = View.VISIBLE
        onInitiate(0)
        Retrofit2x.postSpi(Gson().toJson(entries), part).enqueue(object : RetrofitCallback() {
            override fun onResponse(response: JsonObject) {
                onFinish(100)
                progressBar_text.visibility = View.INVISIBLE
                messageDialog(6, getString(R.string.popup_write_success), false)
                file?.let {
                    if (it.exists()) it.delete()
                }
            }

            override fun onFailure(throwable: Throwable) {
                onError()
                progressBar_text.visibility = View.INVISIBLE
                messageDialog(7, throwable.message, false)
                throwable.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        nfcUtil.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        onInitiate(0)
    }

    override fun onInitiate(percentage: Int) {
        progressBar.progress = percentage
        progressDrawable.setTint(YELLOW)
    }

    override fun onProgress(percentage: Int) {
        progressBar.progress = percentage
    }

    override fun onError() {
        progressDrawable.setTint(RED)
    }

    override fun onFinish(percentage: Int) {
        progressBar.progress = percentage
        progressDrawable.setTint(GREEN)
    }
}
