package kr.djspi.pipe01.tab

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
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
    private var imageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            val listener = context as OnRecordListener
            json = listener.jsonObject
            imageUri = listener.uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_info, container, false)
        setInfo(view)
        setPhoto(view)
        return view
    }

    private fun setInfo(view: View) {
        val hDirection: String = getHorizontalDirection(json)
        val vDirection: String = getVerticalDirection(json)

        if (json["shape"].asString == "제수변") {
            if (hDirection == "" && vDirection == "") {
                view.findViewById<TextView>(R.id.txt_contents).text = fromHtml(
                    getString(
                        R.string.nfc_info_read_contents_valve_alt,
                        json["pipe"].asString,
                        json["spec"].asString.replace("^", " "),
                        json["unit"].asString,
                        json["material"].asString.replace("^", " "),
                        json["shape"].asString,
                        json["spi_type"].asString,
                        json["depth"].asString
                    )
                )
            } else {
                view.findViewById<TextView>(R.id.txt_contents).text = fromHtml(
                    getString(
                        R.string.nfc_info_read_contents_valve,
                        json["pipe"].asString,
                        json["spec"].asString.replace("^", " "),
                        json["unit"].asString,
                        json["material"].asString.replace("^", " "),
                        json["shape"].asString,
                        hDirection,
                        vDirection,
                        json["depth"].asString
                    )
                )
            }
        } else {
            if (hDirection == "" && vDirection == "") {
                view.findViewById<TextView>(R.id.txt_contents).text = fromHtml(
                    getString(
                        R.string.nfc_info_read_contents_alt,
                        json["pipe"].asString,
                        json["shape"].asString,
                        json["spec"].asString.replace("^", " "),
                        json["unit"].asString,
                        json["material"].asString.replace("^", " "),
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
                        json["spec"].asString.replace("^", " "),
                        json["unit"].asString,
                        json["material"].asString.replace("^", " "),
                        hDirection,
                        vDirection,
                        json["depth"].asString
                    )
                )
            }
        }

        try {
            if (json.get("spi_memo") != JsonNull.INSTANCE) {
                if (json.get("spi_memo") != null) {
                    view.findViewById<TextView>(R.id.txt_memo)
                        .setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                    view.findViewById<TextView>(R.id.txt_memo).text = json["spi_memo"].asString
                }
            }
        } catch (ignore: NullPointerException) {
        }
    }

    private fun setPhoto(view: View) {
        try {
            val imageView: ImageView = view.findViewById(R.id.img_photo)
            var requestBuilder: RequestBuilder<Drawable>? = null
            val photoObj = SpiPhotoObject()
            if (imageUri != null) {
                requestBuilder = Glide.with(view).load(imageUri)
                photoObj.setUri(imageUri)
            } else {
                if (json.get("spi_photo_url") != null) {
                    if (!json.get("spi_photo_url").isJsonNull) {
                        requestBuilder = Glide.with(view).load(json["spi_photo_url"].asString)
                        photoObj.url = json["spi_photo_url"].asString
                    }
                }
            }
            if (requestBuilder != null) {
                requestBuilder.fitCenter()
                    .error(R.drawable.ic_photo_error)
                    .dontAnimate()
                    .into(imageView)
                imageView.setOnClickListener {
                    val imageDialog = ImageDialog()
                    val bundle = Bundle(1)
                    bundle.putSerializable("PhotoObj", photoObj)
                    imageDialog.arguments = bundle
                    imageDialog.show(parentFragmentManager, TAG_PHOTO)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    companion object {
        fun getHorizontalDirection(json: JsonObject): String {
            return when (json["position"].asInt) {
                1, 2, 3 -> "차도 방향 ${json["vertical"].asString} m"
                7, 8, 9 -> {
                    when (json["spi_type"].asString) {
                        "표지판" -> {
                            "보도 방향 ${json["vertical"].asString} m"
                        }

                        "표지기" -> {
                            "도로후면 방향 ${json["vertical"].asString} m"
                        }

                        else -> {
                            "차도반대측 방향 ${json["vertical"].asString} m"
                        }
                    }
                }

                else -> ""
            }
        }

        fun getVerticalDirection(json: JsonObject): String {
            return when (json["position"].asInt) {
                1, 4, 7 -> "좌측 ${json["horizontal"].asString} m"
                3, 6, 9 -> "우측 ${json["horizontal"].asString} m"
                else -> ""
            }
        }
    }
}
