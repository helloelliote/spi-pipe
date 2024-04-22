package kr.djspi.pipe01.tab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import kr.djspi.pipe01.BaseActivity
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.databinding.TabPlaneBinding
import kr.djspi.pipe01.geolocation.GeoTrans.CoodinateName.Companion.parseCoordinateName

class PlaneTab : Fragment() {

    private var _binding: TabPlaneBinding? = null
    private val binding get() = _binding!!
    private lateinit var json: JsonObject
    private lateinit var resId: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRecordListener) {
            json = (context as OnRecordListener).jsonObject
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabPlaneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        json["file_plane"].asString?.let {
            resId = it.replace(".png", "_distance")
            val imageView = binding.planeImageView
            imageView.setImageResource(
                resources.getIdentifier(
                    resId,
                    "drawable",
                    BaseActivity.defPackage
                )
            )
        }
        binding.textPlaneHorizontal.text = json["horizontal"].asString
        binding.textPlaneVertical.text = json["vertical"].asString
        setPosition()
        if (json["origin"] != null) {
            if (!json["origin"].isJsonNull) {
                binding.layTableSurveyPipe.apply {
                    visibility = VISIBLE
                    binding.textPlaneOrigin.text = parseCoordinateName(json["origin"].asString)
                    binding.textPlaneX.text = json["coordinate_x"].asString
                    binding.textPlaneY.text = json["coordinate_y"].asString
                }
            }
        }
        if (json["spi_origin"] != null) {
            if (!json["spi_origin"].isJsonNull) {
                binding.layTableSurveySpi.apply {
                    visibility = VISIBLE
                    binding.textPlaneOriginSpi.text = parseCoordinateName(json["spi_origin"].asString)
                    binding.textPlaneXSpi.text = json["spi_coordinate_x"].asString
                    binding.textPlaneYSpi.text = json["spi_coordinate_y"].asString
                }
            }
        }
//        val textView = view.findViewById<TextView>(R.id.lay_elb)
//        if (json["shape"].asString == PipeShape.PipeShapeEnum.엘보형135.type) {
//            textView.visibility = VISIBLE
//        } else {
//            textView.visibility = GONE
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPosition() {
        val positionInt = json["position"].asInt
        when (json["shape"].asString) {
            "직진형" -> {
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
                        binding.textPlaneHorizontal.visibility = GONE
                        binding.textPlaneVertical.visibility = GONE
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
            }

            "십자형" -> {
                when (positionInt) {
                    1 -> setTranslation(vY = -100.0f, hX = -170.0f, hY = -350.0f)
                    2 -> setTranslation(noH = true, vY = -50.0f, hX = 0.0f, hY = 0.0f)
                    3 -> setTranslation(vY = -100.0f, hX = 175.0f, hY = -350.0f)
                    4 -> setTranslation(noV = true, vY = 0.0f, hX = -90.0f, hY = 0.0f)
                    5 -> {
                        binding.textPlaneHorizontal.visibility = GONE
                        binding.textPlaneVertical.visibility = GONE
                    }

                    6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                    7 -> setTranslation(vY = 90.0f, hX = -170.0f, hY = 350.0f)
                    8 -> setTranslation(noH = true, vY = 50.0f, hX = 0.0f, hY = 0.0f)
                    9 -> setTranslation(vY = 95.0f, hX = 175.0f, hY = 350.0f)
                }
            }

            else -> {
                when (positionInt) {
                    1 -> setTranslation(vY = -100.0f, hX = -170.0f, hY = -350.0f)
                    2 -> setTranslation(noH = true, vY = -100.0f, hX = 0.0f, hY = 0.0f)
                    3 -> setTranslation(vY = -100.0f, hX = 175.0f, hY = -350.0f)
                    4 -> setTranslation(noV = true, vY = 0.0f, hX = -90.0f, hY = 0.0f)
                    5 -> {
                        binding.textPlaneHorizontal.visibility = GONE
                        binding.textPlaneVertical.visibility = GONE
                    }

                    6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                    7 -> setTranslation(vY = 90.0f, hX = -170.0f, hY = 350.0f)
                    8 -> setTranslation(noH = true, vY = 95.0f, hX = 0.0f, hY = 0.0f)
                    9 -> setTranslation(vY = 95.0f, hX = 175.0f, hY = 350.0f)
                }
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
            binding.textPlaneVertical.visibility = GONE
            binding.textPlaneHorizontal.visibility = VISIBLE
        }
        if (noH) {
            binding.textPlaneHorizontal.visibility = GONE
            binding.textPlaneVertical.visibility = VISIBLE
        }
        if (noV && noH) {
            binding.textPlaneHorizontal.visibility = GONE
            binding.textPlaneVertical.visibility = GONE
        }
        binding.textPlaneHorizontal.translationX = hX * screenRatio
        binding.textPlaneHorizontal.translationY = hY * screenRatio
        binding.textPlaneVertical.translationY = vY * screenRatio
    }
}
