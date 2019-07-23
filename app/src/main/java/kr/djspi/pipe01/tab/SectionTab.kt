package kr.djspi.pipe01.tab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.tab_plane.*
import kotlinx.android.synthetic.main.tab_preview.*
import kr.djspi.pipe01.BaseActivity
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.R

class SectionTab : Fragment() {

    private lateinit var json: JsonObject

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            json = (context as OnRecordListener).jsonObject
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_section, container, false)
        json["file_section"].asString?.let {
            val resId = it.replace(".png", "")
            planeImageView.setImageResource(
                resources.getIdentifier(
                    resId,
                    "drawable",
                    BaseActivity.defPackage
                )
            )
        }
        text_plane_vertical.text = json["vertical"].asString
        text_depth.text = json["depth"].asString
        text_spec.text =
            "${json["header"].asString} ${json["spec"].asString} ${json["unit"].asString}"
        text_material.text = json["material"].asString
        when (json["position"].asInt) {
            1, 2, 3 -> setTranslation(false, -355.0f)
            4, 5, 6 -> {
                text_spec.translationX = 175.0f
                text_material.translationX = 175.0f
                setTranslation(true)
            }
            7, 8, 9 -> setTranslation(false, 355.0f)
        }
        return view
    }

    private fun setTranslation(noV: Boolean, dX: Float = 0.0f) {
        if (noV) text_plane_vertical.visibility = View.GONE
        text_depth.translationX = dX * screenRatio
        text_depth.translationY = 77.5f * screenRatio
        text_plane_vertical.translationY = -475.0f * screenRatio
        text_spec.translationY = 300.0f * screenRatio
        text_material.translationY = 400.0f * screenRatio
    }
}
