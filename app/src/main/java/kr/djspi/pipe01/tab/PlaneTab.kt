package kr.djspi.pipe01.tab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kr.djspi.pipe01.BaseActivity
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.PipeShape
import kr.djspi.pipe01.geolocation.GeoTrans.CoodinateName.Companion.parseCoordinateName

class PlaneTab : Fragment() {

    private lateinit var json: JsonObject
    private lateinit var resId: String
    private lateinit var horizontal: TextView
    private lateinit var vertical: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            json = (context as OnRecordListener).jsonObject
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_plane, container, false)
        json["file_plane"].asString?.let {
            resId = it.replace(".png", "_distance")
            val imageView = view.findViewById<ImageView>(R.id.planeImageView)
            imageView.setImageResource(
                resources.getIdentifier(
                    resId,
                    "drawable",
                    BaseActivity.defPackage
                )
            )
        }
        horizontal = view.findViewById(R.id.text_plane_horizontal)
        horizontal.text = json["horizontal"].asString
        vertical = view.findViewById(R.id.text_plane_vertical)
        vertical.text = json["vertical"].asString
        setPosition()
        if (json["origin"] != null) {
            if (!json["origin"].isJsonNull) {
                view.findViewById<TableLayout>(R.id.lay_table_survey).apply {
                    visibility = VISIBLE
                    findViewById<TextView>(R.id.text_plane_origin).text =
                        parseCoordinateName(json["origin"].asString)
                    findViewById<TextView>(R.id.text_plane_x).text =
                        json["coordinate_x"].asString
                    findViewById<TextView>(R.id.text_plane_y).text =
                        json["coordinate_y"].asString
                }
            }
        }
        val textView = view.findViewById<TextView>(R.id.lay_elb)
        if (json["shape"].asString == PipeShape.PipeShapeEnum.엘보형135.type) {
            textView.visibility = VISIBLE
        } else {
            textView.visibility = GONE
        }
        return view
    }

    private fun setPosition() {
        val positionInt = json["position"].asInt
        if (json["shape"].asString == "직진형") {
            when (positionInt) {
                1 -> setTranslation(noV = true, vY = 0.0f, hX = -50.0f, hY = 0.0f)
                2 -> {
                    if (resId == "plan_plate_str_2_out_distance") {
                        setTranslation(noV = true, noH = true, vY = 0.0f, hX = 0.0f, hY = 0.0f)
                    } else {
                        setTranslation(noH = true, vY = -50.0f, hX = 0.0f, hY = 0.0f)
                    }
                }
                3 -> setTranslation(noV = true, vY = 0.0f, hX = 50.0f, hY = 0.0f)
                4 -> setTranslation(noV = true, vY = 0.0f, hX = -100.0f, hY = 0.0f)
                5 -> {
                    horizontal.visibility = GONE
                    vertical.visibility = GONE
                }
                6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                7 -> setTranslation(noV = true, vY = 0.0f, hX = -50.0f, hY = 0.0f)
                8 -> {
                    if (resId == "plan_plate_str_8_out_distance") {
                        setTranslation(noV = true, noH = true, vY = 0.0f, hX = 0.0f, hY = 0.0f)
                    } else {
                        setTranslation(noH = true, vY = 50.0f, hX = 0.0f, hY = 0.0f)
                    }
                }
                9 -> setTranslation(noV = true, vY = 0.0f, hX = 50.0f, hY = 0.0f)
            }
        } else {
            when (positionInt) {
                1 -> setTranslation(vY = -100.0f, hX = -170.0f, hY = -350.0f)
                2 -> setTranslation(noH = true, vY = -100.0f, hX = 0.0f, hY = 0.0f)
                3 -> setTranslation(vY = -100.0f, hX = 175.0f, hY = -350.0f)
                4 -> setTranslation(noV = true, vY = 0.0f, hX = -90.0f, hY = 0.0f)
                5 -> {
                    horizontal.visibility = GONE
                    vertical.visibility = GONE
                }
                6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                7 -> setTranslation(vY = 90.0f, hX = -170.0f, hY = 350.0f)
                8 -> setTranslation(noH = true, vY = 95.0f, hX = 0.0f, hY = 0.0f)
                9 -> setTranslation(vY = 95.0f, hX = 175.0f, hY = 350.0f)
            }
        }
    }

    private fun setTranslation(
        noV: Boolean = false,
        noH: Boolean = false,
        vY: Float,
        hX: Float,
        hY: Float
    ) {
        if (noV) {
            vertical.visibility = GONE
            horizontal.visibility = VISIBLE
        }
        if (noH) {
            horizontal.visibility = GONE
            vertical.visibility = VISIBLE
        }
        if (noV && noH) {
            horizontal.visibility = GONE
            vertical.visibility = GONE
        }
        horizontal.translationX = hX * screenRatio
        horizontal.translationY = hY * screenRatio
        vertical.translationY = vY * screenRatio
    }
}
