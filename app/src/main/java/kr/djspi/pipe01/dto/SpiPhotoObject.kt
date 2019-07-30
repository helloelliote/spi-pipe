package kr.djspi.pipe01.dto

import android.net.Uri
import java.io.File
import java.io.Serializable

data class SpiPhotoObject(var file: File? = null) : DataItem, Serializable {

    var url: String? = null
    var uri: String? = null

    fun getUri(): Uri? {
        return if (uri == null) null
        else Uri.parse(uri)
    }

    fun setUri(uri: Uri?) {
        if (uri == null) this.uri = null
        else this.uri = uri.toString()
    }
}
