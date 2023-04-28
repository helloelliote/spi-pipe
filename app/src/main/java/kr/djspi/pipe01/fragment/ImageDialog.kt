package kr.djspi.pipe01.fragment

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.SpiPhotoObject
import java.nio.charset.Charset
import java.security.MessageDigest

class ImageDialog : DialogFragment() {

    private var transformation: RotateTransformation? = null
    private var imageUri: Uri? = null
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar)
        arguments?.let {
            val photoObj: SpiPhotoObject = it.getSerializable("PhotoObj") as SpiPhotoObject
            return@let photoObj.let { photoObject ->
                imageUri = photoObject.getUri()
                imageUrl = photoObject.url
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_view, container, false)
        var requestBuilder: RequestBuilder<Drawable>? = null
        if (imageUri != null) {
            requestBuilder = Glide.with(view).load(imageUri).override(SIZE_ORIGINAL, SIZE_ORIGINAL)
        } else if (imageUrl != null) {
            requestBuilder = Glide.with(view).load(imageUrl).override(SIZE_ORIGINAL, SIZE_ORIGINAL)
        }
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        if (transformation == null) transformation = RotateTransformation(90f)
        requestBuilder?.transform(CenterInside(), transformation)?.into(imageView)?.clearOnDetach()
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        imageUri = null
        imageUrl = null
        transformation = null
    }

    /**
     * Must implement equals() and hashCode() for memory caching to work correctly.
     */
    inner class RotateTransformation(private val rotationAngle: Float) : BitmapTransformation() {

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            val matrix = Matrix()
            val width = toTransform.width
            val height = toTransform.height
            if (width > height) {
                matrix.postRotate(rotationAngle)
            } else {
                matrix.postRotate(0f)
            }
            return Bitmap.createBitmap(toTransform, 0, 0, width, height, matrix, true)
        }

        override fun equals(other: Any?): Boolean = other is RotateTransformation

        override fun hashCode(): Int = javaClass.name.hashCode()

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(javaClass.name.toByteArray(Charset.forName("UTF-8")))
        }
    }
}
