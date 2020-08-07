package kr.djspi.pipe01.util

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object ImageUtil {

    private const val DATE_PATTERN = "yyyyMMdd_HHmmss_SSS"
    private const val REGEX_IMAGE_EXT = "((\\.(?i)(jpg|jpeg|tif|tiff|webp|png|gif|bmp))$)"

    fun Activity.uriToFilePath(uri: Uri?): String {
        val cursor = contentResolver.query(uri!!, arrayOf("_data"), null, null, null)
        var path = ""
        cursor?.use {
            it.moveToNext()
            path = it.getString(it.getColumnIndex("_data"))
        }
        return path
    }

    fun File.resizeImageToRes(maxResolution: Int): File {
        val timeStamp = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
        val newFile = File(this.parent, "IMG_$timeStamp.jpg")
        var bitmap = decodeFile(this.path)
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val options = BitmapFactory.Options()
        options.inSampleSize = when ((if (width > height) width else height).roundToInt()) {
            in 1..1536 -> 1
            in 1537..3072 -> 2
            else -> 4
        }
        bitmap = decodeFile(this.path, options)
        return FileOutputStream(newFile).use { outputStream ->
            if (width < maxResolution.toFloat() && height < maxResolution.toFloat()) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                return@use newFile
            } else {
                val bitmapRatio = width / height
                var newWidth = maxResolution
                var newHeight = maxResolution
                if (1.0f > bitmapRatio) {
                    newWidth = (maxResolution.toFloat() * bitmapRatio).toInt()
                } else {
                    newHeight = (maxResolution.toFloat() / bitmapRatio).toInt()
                }
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                return@use newFile
            }
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

    fun Activity.saveImageToGallery(oldFile: File, folderName: String) {
        val bitmap = decodeFile(oldFile.path, null)
        val timeStamp = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.IS_PENDING, true)
            }
            val uri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, contentResolver.openOutputStream(uri))
//                    .apply {
//                        // TODO: EXIF 전달 테스트
//                        uriToFile(context, uri).preserveExifOf(oldFile)
//                    }
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "Pictures" + File.separator + folderName
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
                .apply {
                    file.preserveExif(oldFile)
                }
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(file.absolutePath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }
        }
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
            }
        }
    }

    fun File.preserveExif(oldFile: File): File {
        copyExif(oldFile.path, this.absolutePath).also {
            return this
        }
    }

    private fun copyExif(oldFilePath: String, newFilePath: String) {
        val oldExif = ExifInterface(oldFilePath)
        val newExif = ExifInterface(newFilePath)
        val buildVersion = Build.VERSION.SDK_INT

        if (buildVersion >= 11) {
            newExif.copyAttribute(oldExif, "FNumber", "ExposureTime", "ISOSpeedRatings")
        }
        if (buildVersion >= 9) {
            newExif.copyAttribute(oldExif, "GPSAltitude", "GPSAltitudeRef")
        }
        if (buildVersion >= 8) {
            newExif.copyAttribute(oldExif, "FocalLength", "GPSDateStamp", "GPSProcessingMethod")
        }
        newExif.copyAttribute(
            oldExif,
            "DateTime",
            "Flash",
            "GPSLatitude",
            "GPSLatitudeRef",
            "GPSLongitude",
            "GPSLongitudeRef",
            "Make",
            "Model",
            "Orientation",
            "WhiteBalance"
        )

        newExif.saveAttributes()
    }

    private fun ExifInterface.copyAttribute(oldExif: ExifInterface, vararg tags: String) {
        tags.forEach {
            if (oldExif.getAttribute(it) != null) {
                this.setAttribute(it, oldExif.getAttribute(it))
            }
        }
    }
}
