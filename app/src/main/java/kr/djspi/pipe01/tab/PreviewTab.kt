package kr.djspi.pipe01.tab

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonObject
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_PREVIEW
import kr.djspi.pipe01.R

class PreviewTab : Fragment(), View.OnClickListener {

    private lateinit var listener: OnRecordListener
    private lateinit var json: JsonObject
    private var imageFileUri: Uri? = null

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

        view.findViewById<TextView>(R.id.text_pipe).text = json["pipe"].asString
        view.findViewById<TextView>(R.id.text_shape).text = json["shape"].asString
        view.findViewById<TextView>(R.id.text_horizontal).text = json["horizontal_form"].asString
        view.findViewById<TextView>(R.id.text_vertical).text = json["vertical_form"].asString
        view.findViewById<TextView>(R.id.text_depth).text = json["depth"].asString
        view.findViewById<TextView>(R.id.header).text = json["header"].asString
        view.findViewById<TextView>(R.id.text_spec).text = json["spec"].asString
        view.findViewById<TextView>(R.id.unit).text = json["unit"].asString
        view.findViewById<TextView>(R.id.text_material).text = json["material"].asString
        view.findViewById<TextView>(R.id.text_supervise).text = json["supervise"].asString
        view.findViewById<TextView>(R.id.text_supervise_contact).text =
            json["supervise_contact"].asString
        view.findViewById<TextView>(R.id.text_memo).text = json["spi_memo"].asString
        view.findViewById<TextView>(R.id.text_construction).text = json["construction"].asString
        view.findViewById<TextView>(R.id.text_construction_contact).text =
            json["construction_contact"].asString

        if (imageFileUri != null) {
            view.findViewById<TextView>(R.id.text_photo).text = getString(R.string.record_photo_ok)
        } else {
            view.findViewById<TextView>(R.id.text_photo).text = null
        }

        view.findViewById<MaterialButton>(R.id.button_next).setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> listener.onRecord(TAG_PREVIEW, RESULT_PASS)
            else -> listener.onRecord(TAG_PREVIEW, RESULT_FAIL)
        }
    }
}
