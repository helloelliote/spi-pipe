package kr.djspi.pipe01.tab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.tab_preview.*
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_PREVIEW
import kr.djspi.pipe01.R

class PreviewTab : Fragment(), View.OnClickListener {

    private lateinit var listener: OnRecordListener
    private lateinit var json: JsonObject
    private var imageFileUri: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            listener = context
            json = listener.jsonObject
            imageFileUri = listener.uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_preview, container, false)

        text_pipe.text = json["pipe"].asString
        text_shape.text = json["shape"].asString
        text_horizontal.text = json["horizontal_form"].asString
        text_vertical.text = json["vertical_form"].asString
        text_depth.text = json["depth"].asString
        header.text = json["header"].asString
        text_spec.text = json["spec"].asString
        unit.text = json["unit"].asString
        text_material.text = json["material"].asString
        text_supervise.text = json["supervise"].asString
        text_supervise_contact.text = json["supervise_contact"].asString
        text_memo.text = json["spi_memo"].asString
        text_construction.text = json["construction"].asString
        text_construction_contact.text = json["construction_contact"].asString

        if (imageFileUri != null) {
            text_photo.text = getString(R.string.record_photo_ok)
        } else {
            text_photo.text = null
        }

        button_confirm.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_confirm -> listener.onRecord(TAG_PREVIEW, RESULT_PASS)
            else -> listener.onRecord(TAG_PREVIEW, RESULT_FAIL)
        }
    }
}
