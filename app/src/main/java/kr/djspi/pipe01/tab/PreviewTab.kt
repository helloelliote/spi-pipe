package kr.djspi.pipe01.tab

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_PREVIEW
import kr.djspi.pipe01.R
import kr.djspi.pipe01.databinding.TabPreviewBinding

class PreviewTab : Fragment(), View.OnClickListener {

    private var _binding: TabPreviewBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textPipe.text = json["pipe"].asString
        binding.textShape.text = json["shape"].asString
        binding.textHorizontal.text = json["horizontal_form"].asString
        binding.textVertical.text = json["vertical_form"].asString
        binding.textDepth.text = json["depth"].asString
        binding.header.text = json["header"].asString
        binding.textSpec.text = json["spec"].asString.replace("^", " ")
        binding.unit.text = json["unit"].asString
        binding.textMaterial.text = json["material"].asString.replace("^", " ")
        binding.textSupervise.text = json["supervise"].asString
        binding.textSuperviseContact.text = json["supervise_contact"].asString
        binding.textMemo.text = json["spi_memo"].asString
        binding.textConstruction.text = json["construction"].asString
        binding.textConstructionContact.text = json["construction_contact"].asString

        if (imageFileUri != null) {
            binding.textPhoto.text = getString(R.string.record_photo_ok)
        } else {
            binding.textPhoto.text = null
        }

        binding.buttonNext.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> listener.onRecord(TAG_PREVIEW, RESULT_PASS)
            else -> listener.onRecord(TAG_PREVIEW, RESULT_FAIL)
        }
    }
}
