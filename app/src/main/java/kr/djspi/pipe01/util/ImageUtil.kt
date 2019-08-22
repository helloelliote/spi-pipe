package kr.djspi.pipe01.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtil {

    private const val DATE_PATTERN = "yyyyMMdd_HHmmss_SSS"
    private const val REGEX_IMAGE_EXT = "((\\.(?i)(jpg|jpeg|tif|tiff|webp|png|gif|bmp))$)"

    private fun uriToFilePath(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf("_data"), null, null, null)
        var path = ""
        cursor?.let {
            it.moveToNext()
            path = it.getString(cursor.getColumnIndex("_data"))
            it.close()
        }
        cursor?.close()
        return path
    }

    fun uriToFile(context: Context, uri: Uri): File = File(uriToFilePath(context, uri))

    @Throws(IOException::class)
    fun prepareFile(context: Context): File {
        val timeStamp = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp"
        val storageDir =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Camera")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun subSample4x(file: File, maxResolution: Int): File {
        val options = BitmapFactory.Options()
        options.inSampleSize = 4
        val sourceBitmap = decodeFile(file.path, null)
        val copyBitmap = sourceBitmap.copy(sourceBitmap.config, true)
        val width = copyBitmap.width.toFloat()
        val height = copyBitmap.height.toFloat()
        val timeStamp = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
        val newFile = File(file.parent, "IMG_$timeStamp.jpg")
        try {
            FileOutputStream(newFile).use { outputStream ->
                if (width < maxResolution.toFloat() && height < maxResolution.toFloat()) {
                    copyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    return newFile
                } else {
                    val bitmapRatio = width / height
                    var newWidth = maxResolution
                    var newHeight = maxResolution
                    if (1.0f > bitmapRatio) {
                        newWidth = (maxResolution.toFloat() * bitmapRatio).toInt()
                    } else {
                        newHeight = (maxResolution.toFloat() / bitmapRatio).toInt()
                    }
                    val resizeBitmap = createScaledBitmap(copyBitmap, newWidth, newHeight, true)
                    resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    return newFile
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return file
        }
    }

    private fun createFileName(fileName: String): String {
        var newFileName = fileName.replace(REGEX_IMAGE_EXT.toRegex(), "_R.jpg")
        while (newFileName.indexOf(".") != newFileName.lastIndexOf(".")) {
            newFileName = newFileName.replaceFirst("(\\.(?i))".toRegex(), "")
        }
        newFileName = newFileName.replace("(₩(?i))".toRegex(), "")
        return newFileName
    }
}
