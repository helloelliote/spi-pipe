package kr.djspi.pipe01.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
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
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMG_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
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

    fun saveImageToGallery(context: Context, oldFile: File, folderName: String) {
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
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
//                    .apply {
//                        // TODO: EXIF 전달 테스트
//                        uriToFile(context, uri).preserveExifOf(oldFile)
//                    }
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory =
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Pictures" + File.separator + folderName
                )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
                .apply {
                    file.preserveExifOf(oldFile)
                }
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(file.absolutePath)
                mediaScanIntent.data = Uri.fromFile(f)
                context.sendBroadcast(mediaScanIntent)
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

    fun File.preserveExifOf(oldFile: File): File {
        copyExif(oldFile.path, this.absolutePath).also {
            return this
        }
    }

    private fun copyExif(oldFilePath: String, newFilePath: String) {
        val oldExif = ExifInterface(oldFilePath)
        val newExif = ExifInterface(newFilePath)
        val buildVersion = Build.VERSION.SDK_INT

        if (buildVersion >= 11) {
            newExif.copyAttributeOf(oldExif, "FNumber", "ExposureTime", "ISOSpeedRatings")
        }
        if (buildVersion >= 9) {
            newExif.copyAttributeOf(oldExif, "GPSAltitude", "GPSAltitudeRef")
        }
        if (buildVersion >= 8) {
            newExif.copyAttributeOf(oldExif, "FocalLength", "GPSDateStamp", "GPSProcessingMethod")
        }
        newExif.copyAttributeOf(
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

    private fun ExifInterface.copyAttributeOf(oldExif: ExifInterface, vararg tags: String) {
        tags.forEach {
            if (oldExif.getAttribute(it) != null) {
                this.setAttribute(it, oldExif.getAttribute(it))
            }
        }
    }
}
