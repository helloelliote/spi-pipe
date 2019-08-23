package kr.djspi.pipe01.tab

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.fragment.ImageDialog
import kr.djspi.pipe01.util.fromHtml

class InfoTab : Fragment() {

    private lateinit var json: JsonObject
    private var imageUri: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            json = (context as OnRecordListener).jsonObject
            imageUri = (context as OnRecordListener).uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_info, container, false)
        setInfo(view)
        return view
    }

    private fun setInfo(view: View) {
        val hDirection: String = when (json["position"].asInt) {
            1, 2, 3 -> "차도 방향 ${json["vertical"].asString} m"
            7, 8, 9 -> {
                if (json["spi_type"].asString == "표지주") {
                    "차도반대측 방향 ${json["vertical"].asString} m"
                } else {
                    "보도 방향 ${json["vertical"].asString} m"
                }
            }
            else -> ""
        }

        val vDirection: String = when (json["position"].asInt) {
            1, 4, 7 -> "좌측 ${json["horizontal"].asString} m"
            3, 6, 9 -> "우측 ${json["horizontal"].asString} m"
            else -> ""
        }

        if (hDirection == "" && vDirection == "") {
            view.findViewById<TextView>(R.id.txt_contents).text = fromHtml(
                getString(
                    R.string.nfc_info_read_contents_alt,
                    json["pipe"].asString,
                    json["shape"].asString,
                    json["spec"].asString,
                    json["unit"].asString,
                    json["material"].asString,
                    json["spi_type"].asString,
                    json["depth"].asString
                )
            )
        } else {
            view.findViewById<TextView>(R.id.txt_contents).text = fromHtml(
                getString(
                    R.string.nfc_info_read_contents,
                    json["pipe"].asString,
                    json["shape"].asString,
                    json["spec"].asString,
                    json["unit"].asString,
                    json["material"].asString,
                    hDirection,
                    vDirection,
                    json["depth"].asString
                )
            )
        }

        try {
            if (json.get("spi_memo") != JsonNull.INSTANCE) {
                view.findViewById<TextView>(R.id.txt_memo)
                    .setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                view.findViewById<TextView>(R.id.txt_memo).text = json["spi_memo"].asString
            }
        } catch (ignore: NullPointerException) {
        }

        // TODO: 사진 표시 안됨
        try {
            var requestBuilder: RequestBuilder<Drawable>? = null
            val photoObj = SpiPhotoObject()
            if (imageUri != null) {
                requestBuilder = Glide.with(view).load(imageUri)
                photoObj.uri = imageUri
            } else {
                if (json.get("spi_photo_url") != null) {
                    if (json["spi_photo_url"] != JsonNull.INSTANCE) {
                        requestBuilder = Glide.with(view).load(json["spi_photo_url"].asString)
                        photoObj.uri = json["spi_photo_url"].asString
                    }
                }
            }
            val imageView = view.findViewById<ImageView>(R.id.img_photo)
            requestBuilder?.apply {
                fitCenter()
                error(R.drawable.ic_photo_error)
                dontAnimate()
                into(imageView)
                imageView.setOnClickListener {
                    val bundle = Bundle(1)
                    bundle.putSerializable("SpiPhotoObject", photoObj)
                    ImageDialog().apply { arguments = bundle }.show(childFragmentManager, TAG_PHOTO)
                }
            }
        } catch (ignore: NullPointerException) {
        }
    }
}