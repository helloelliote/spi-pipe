package kr.djspi.pipe01.tab

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.R
import kr.djspi.pipe01.databinding.TabInfoBinding
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.fragment.ImageDialog
import kr.djspi.pipe01.util.fromHtml

class InfoTab : Fragment() {

    private var _binding: TabInfoBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInfo()
        setPhoto()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setInfo() {
        val hDirection: String = getHorizontalDirection(json)
        val vDirection: String = getVerticalDirection(json)

        binding.txtContents.text =
            if (json["shape"].asString == "제수변") {
                if (hDirection == "" && vDirection == "") {
                    fromHtml(
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
                    fromHtml(
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
                    fromHtml(
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
                    fromHtml(
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
                    binding.txtMemo.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                    binding.txtMemo.text = json["spi_memo"].asString
                }
            }
        } catch (ignore: NullPointerException) {
        }
    }

    private fun setPhoto() {
        try {
            val imageView = binding.imgPhoto
            var requestBuilder: RequestBuilder<Drawable>? = null
            val photoObj = SpiPhotoObject()
            if (imageUri != null) {
                requestBuilder = Glide.with(imageView).load(imageUri)
                photoObj.setUri(imageUri)
            } else {
                if (json.get("spi_photo_url") != null) {
                    if (!json.get("spi_photo_url").isJsonNull) {
                        requestBuilder = Glide.with(imageView).load(json["spi_photo_url"].asString)
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
