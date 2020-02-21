package kr.djspi.pipe01.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.andreabaccega.widget.FormEditText
import kr.djspi.pipe01.BaseActivity.Companion.defPackage
import kr.djspi.pipe01.BaseActivity.Companion.screenRatio
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.R
import kr.djspi.pipe01.util.DecimalFilter

class DistanceDialog : DialogFragment(), View.OnClickListener {

    private var position = -1
    private var dialogTitle: String? = null
    private lateinit var shape: String
    private lateinit var plan: String
    private lateinit var resId: String
    private lateinit var horizontal: FormEditText
    private lateinit var vertical: FormEditText
    private lateinit var listener: OnSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val bundle = it
            shape = bundle.getString("shapeString")!!
            plan = bundle.getString("planString")!!
            position = bundle.getInt("positionInt")
            resId = "${plan}_distance"
        }
        dialogTitle = getString(R.string.popup_title_input_distance)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plot_distance, container, false)
        view.findViewById<TextView>(R.id.popup_title).text = dialogTitle
        view.findViewById<ImageView>(R.id.lay_background).apply {
            setImageDrawable(fromRes(resId))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        horizontal = view.findViewById(R.id.form_horizontal)
        vertical = view.findViewById(R.id.form_vertical)
        val filter = arrayOf<InputFilter>(DecimalFilter(4, 2))
        horizontal.filters = filter
        vertical.filters = filter
        arrayOf<View>(
            view.findViewById(R.id.button_close),
            view.findViewById(R.id.btn_cancel),
            view.findViewById(R.id.btn_ok)
        ).forEach {
            it.setOnClickListener(this)
        }
        setPosition()
        return view
    }

    private fun fromRes(resId: String): Drawable {
        return resources.getDrawable(
            resources.getIdentifier(resId, "drawable", defPackage), null
        )
    }

    private fun setPosition() {
        if (shape == "직진형") {
            when (position) {
                1 -> setTranslation(noV = true, vY = 0.0f, hX = -50.0f, hY = 0.0f)
                2 -> {
                    if (plan == "plan_plate_str_2_out") {
                        listener.onSelect(TAG_DISTANCE, 0, "0.00", "0.00")
                        dismissAllowingStateLoss()
                    } else {
                        setTranslation(noH = true, vY = -65.0f, hX = 0.0f, hY = 0.0f)
                    }
                }
                3 -> setTranslation(noV = true, vY = 0.0f, hX = 50.0f, hY = 0.0f)
                4 -> setTranslation(noV = true, vY = 0.0f, hX = -100.0f, hY = 0.0f)
                5 -> { // Unreachable case
                }
                6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                7 -> setTranslation(noV = true, vY = 0.0f, hX = -50.0f, hY = 0.0f)
                8 -> {
                    if (plan == "plan_plate_str_8_out") {
                        listener.onSelect(TAG_DISTANCE, 0, "0.0", "0.0")
                        dismissAllowingStateLoss()
                    } else {
                        setTranslation(noH = true, vY = 65.0f, hX = 0.0f, hY = 0.0f)
                    }
                }
                9 -> setTranslation(noV = true, vY = 0.0f, hX = 50.0f, hY = 0.0f)
            }
        } else {
            when (position) {
                1 -> setTranslation(vY = -90.0f, hX = -150.0f, hY = -280.0f)
                2 -> setTranslation(noH = true, vY = -90.0f, hX = 0.0f, hY = 0.0f)
                3 -> setTranslation(vY = -90.0f, hX = 155.0f, hY = -280.0f)
                4 -> setTranslation(noV = true, vY = 0.0f, hX = -100.0f, hY = 0.0f)
                5 -> { // Unreachable case
                }
                6 -> setTranslation(noV = true, vY = 0.0f, hX = 100.0f, hY = 0.0f)
                7 -> setTranslation(vY = 90.0f, hX = -150.0f, hY = 280.0f)
                8 -> setTranslation(noH = true, vY = 95.0f, hX = 0.0f, hY = 0.0f)
                9 -> setTranslation(vY = 95.0f, hX = 155.0f, hY = 280.0f)
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
        when {
            noV -> {
                vertical.setText("0.0")
                vertical.visibility = GONE
                horizontal.visibility = VISIBLE
            }
            noH -> {
                horizontal.setText("0.0")
                horizontal.visibility = GONE
                vertical.visibility = VISIBLE
            }
            else -> {
                horizontal.visibility = VISIBLE
                vertical.visibility = VISIBLE
            }
        }
        vertical.translationY = vY * screenRatio
        horizontal.translationX = hX * screenRatio
        horizontal.translationY = hY * screenRatio
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> if (isAllValid()) {
                listener.onSelect(
                    TAG_DISTANCE,
                    0,
                    horizontal.text.toString(),
                    vertical.text.toString()
                )
                horizontal.visibility = VISIBLE
                vertical.visibility = VISIBLE
                dismissAllowingStateLoss()
            } else return
            R.id.btn_cancel -> {
                horizontal.visibility = VISIBLE
                vertical.visibility = VISIBLE
                listener.onSelect(TAG_DISTANCE, -2, null)
                dismissAllowingStateLoss()
            }
            R.id.button_close -> dismissAllowingStateLoss()
        }
    }

    private fun isAllValid(): Boolean {
        var allValid = true
        val validateFields = arrayOf(horizontal, vertical)
        for (field in validateFields) {
            allValid = field.testValidity() && allValid
        }
        return allValid
    }
}
