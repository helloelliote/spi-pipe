package kr.djspi.pipe01.network

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressBody(
    private val file: File,
    private val contentType: String,
    val callback: UploadCallback,
    private val buffer: Int = 2048
) : RequestBody() {

    override fun contentType(): MediaType? = "$contentType/*".toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalSize = file.length()
        val buffer = ByteArray(buffer)
        FileInputStream(file).use {
            var number = 0
            var uploadSize = 0L
            val readSize = it.read(buffer)
            val handler = Handler(Looper.getMainLooper())
            while (readSize != -1) {
                val progress = (100 * uploadSize / totalSize).toInt()
                if (progress > number + 1) {
                    // update progress on UI thread
                    handler.post(OnProgressUpdate(uploadSize, totalSize))
                    number = progress
                }
                uploadSize += readSize.toLong()
                sink.write(buffer, 0, readSize)
            }
        }
    }

    interface UploadCallback {

        fun onInitiate(percentage: Int)

        fun onProgress(percentage: Int)

        fun onError()

        fun onFinish(percentage: Int)
    }

    inner class OnProgressUpdate(private val upload: Long, private val total: Long) : Runnable {

        override fun run() = callback.onProgress((100 * upload / total).toInt())
    }
}
