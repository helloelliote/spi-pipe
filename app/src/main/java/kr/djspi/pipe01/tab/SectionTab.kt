package kr.djspi.pipe01.tab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kr.djspi.pipe01.BaseActivity
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.R

class SectionTab : Fragment() {

    private lateinit var json: JsonObject
    private lateinit var vertical: TextView
    private lateinit var depth: TextView
    private lateinit var spec: TextView
    private lateinit var material: TextView

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
            val imageView = view.findViewById<ImageView>(R.id.planeImageView)
            imageView.setImageResource(
                resources.getIdentifier(
                    resId,
                    "drawable",
                    BaseActivity.defPackage
                )
            )
        }
        vertical = view.findViewById(R.id.text_plane_vertical)
        vertical.text = json["vertical"].asString
        depth = view.findViewById(R.id.text_depth)
        depth.text = json["depth"].asString
        spec = view.findViewById(R.id.text_spec)
        spec.text =
            "${json["header"].asString} ${json["spec"].asString.replace("^", " ")} ${json["unit"].asString}"
        material = view.findViewById(R.id.text_material)
        material.text = json["material"].asString.replace("^", " ")
        when (json["position"].asInt) {
            1, 2, 3 -> setTranslation(false, -355.0f)
            4, 5, 6 -> {
                spec.translationX = 175.0f
                material.translationX = 175.0f
                setTranslation(true)
            }
            7, 8, 9 -> setTranslation(false, 355.0f)
        }
        return view
    }

    private fun setTranslation(noV: Boolean, dX: Float = 0.0f) {
        if (noV) vertical.visibility = View.GONE
        depth.translationX = dX * screenRatio
        depth.translationY = 77.5f * screenRatio
        vertical.translationY = -475.0f * screenRatio
        spec.translationY = 300.0f * screenRatio
        material.translationY = 400.0f * screenRatio
    }
}
