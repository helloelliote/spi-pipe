package kr.djspi.pipe01.network

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ProgressBody(
    private val file: File,
    private val contentType: String,
    private val callback: UploadCallback
) :
    RequestBody() {
    override fun contentLength(): Long {
        return file.length()
    }

    override fun contentType(): MediaType? {
        return "$contentType/*".toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val totalSize = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        FileInputStream(file).use { inputStream ->
            var uploadSize = 0L
            var readSize: Int
            var number = 0
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { readSize = it } != -1) {
                val progress = (100 * uploadSize / totalSize).toInt()
                if (progress > number + 1) {
                    // update progress on UI thread

                    handler.post(ProgressUpdater(uploadSize, totalSize))
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

    private inner class ProgressUpdater(
        private val uploadSize: Long,
        private val totalSize: Long
    ) :
        Runnable {
        override fun run() {
            callback.onProgress((100 * uploadSize / totalSize).toInt())
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
