package kr.djspi.pipe01.tab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kr.djspi.pipe01.BaseActivity
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.databinding.TabSectionBinding

class SectionTab : Fragment() {

    private var _binding: TabSectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var json: JsonObject

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            json = (context as OnRecordListener).jsonObject
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        json["file_section"].asString?.let {
            val resId = it.replace(".png", "")
            binding.planeImageView.setImageResource(
                resources.getIdentifier(
                    resId,
                    "drawable",
                    BaseActivity.defPackage
                )
            )
        }
        binding.textPlaneVertical.text = json["vertical"].asString
        binding.textDepth.text = json["depth"].asString
        binding.textSpec.text =
            "${json["header"].asString} ${json["spec"].asString.replace("^", " ")} ${json["unit"].asString}"
        binding.textMaterial.text = json["material"].asString.replace("^", " ")
        when (json["position"].asInt) {
            1, 2, 3 -> setTranslation(false, -355.0f)
            4, 5, 6 -> {
                binding.textSpec.translationX = 175.0f
                binding.textMaterial.translationX = 175.0f
                setTranslation(true)
            }

            7, 8, 9 -> setTranslation(false, 355.0f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setTranslation(noV: Boolean, dX: Float = 0.0f) {
        if (noV) binding.textPlaneVertical.visibility = View.GONE
        binding.textDepth.translationX = dX * screenRatio
        binding.textDepth.translationY = 77.5f * screenRatio
        binding.textPlaneVertical.translationY = -475.0f * screenRatio
        binding.textSpec.translationY = 300.0f * screenRatio
        binding.textMaterial.translationY = 400.0f * screenRatio
    }
}
