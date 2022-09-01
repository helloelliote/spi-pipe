package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kr.djspi.pipe01.Const
import kr.djspi.pipe01.R
import kr.djspi.pipe01.SpiLocationActivity.Companion.originPoint
import kr.djspi.pipe01.geolocation.GeoTrans
import kr.djspi.pipe01.util.DecimalFilter

class SurveyDialog3 : DialogFragment(), View.OnClickListener {

    private var selectIndex: Int = -1
    private var savedCheckedIndex: Int = -1
    private lateinit var inputXPipe: TextInputEditText
    private lateinit var inputYPipe: TextInputEditText
    private lateinit var layXPipe: TextInputLayout
    private lateinit var layYPipe: TextInputLayout
    private lateinit var inputXSpi: TextInputEditText
    private lateinit var inputYSpi: TextInputEditText
    private lateinit var layXSpi: TextInputLayout
    private lateinit var layYSpi: TextInputLayout
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
            savedCheckedIndex = it.getInt("savedCheckedIndex")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_survey_new, container, false)
        val nmapRadiogroup = view.findViewById<RadioGroup>(R.id.nmap_radioGroup)
        nmapRadiogroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            selectIndex = group.indexOfChild(checkedRadioButton)
            when (checkedId) {
                R.id.nmap_radio_central -> originPoint =
                    GeoTrans.Coordinate.GRS80_MIDDLE_WITH_JEJUDO
                R.id.nmap_radio_east -> originPoint = GeoTrans.Coordinate.GRS80_EAST
                R.id.nmap_radio_eastsea -> originPoint = GeoTrans.Coordinate.GRS80_EASTSEA
                R.id.nmap_radio_west -> originPoint = GeoTrans.Coordinate.GRS80_WEST
            }
        }
        if (savedCheckedIndex > -1) {
            val checkedRadioButton = nmapRadiogroup.getChildAt(savedCheckedIndex) as RadioButton
            checkedRadioButton.isChecked = true
        }
        inputXPipe = view.findViewById(R.id.input_x_pipe)
        inputYPipe = view.findViewById(R.id.input_y_pipe)
        inputXSpi = view.findViewById(R.id.input_x_spi)
        inputYSpi = view.findViewById(R.id.input_y_spi)
        inputXPipe.filters = arrayOf(DecimalFilter(10, 4))
        inputYPipe.filters = arrayOf(DecimalFilter(10, 4))
        inputXSpi.filters = arrayOf(DecimalFilter(10, 4))
        inputYSpi.filters = arrayOf(DecimalFilter(10, 4))
        layXPipe = view.findViewById(R.id.lay_x_pipe)
        layYPipe = view.findViewById(R.id.lay_y_pipe)
        layXSpi = view.findViewById(R.id.lay_x_spi)
        layYSpi = view.findViewById(R.id.lay_y_spi)
        layXPipe.error = null
        layYPipe.error = null
        layXSpi.error = null
        layYSpi.error = null
        arrayOf<View>(
            view.findViewById(R.id.button_dismiss),
            view.findViewById(R.id.btn_ok)
        ).forEach {
            it.setOnClickListener(this)
        }
        return view
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.btn_ok -> {
                if (selectIndex == -1) {
                    layXPipe.error = getString(R.string.popup_survey_origin)
                    layYPipe.error = getString(R.string.popup_survey_origin)
                    layXSpi.error = getString(R.string.popup_survey_origin)
                    layYSpi.error = getString(R.string.popup_survey_origin)
                    return
                }
                val isInputXPipeValid = validate(layXPipe, inputXPipe)
                val isInputYPipeValid = validate(layYPipe, inputYPipe)
                val isInputXSpiValid = validate(layXSpi, inputXSpi)
                val isInputYSpiValid = validate(layYSpi, inputYSpi)
                when {
                    (isInputXPipeValid && !isInputYPipeValid) && (!isInputXSpiValid && !isInputYSpiValid) -> {
                        layXSpi.error = null
                        layYSpi.error = null
                        return
                    }
                    (!isInputXPipeValid && isInputYPipeValid) && (!isInputXSpiValid && !isInputYSpiValid) -> {
                        layXSpi.error = null
                        layYSpi.error = null
                        return
                    }
                    (!isInputXPipeValid && !isInputYPipeValid) && (isInputXSpiValid && !isInputYSpiValid) -> {
                        layXPipe.error = null
                        layYPipe.error = null
                        return
                    }
                    (!isInputXPipeValid && !isInputYPipeValid) && (!isInputXSpiValid && isInputYSpiValid) -> {
                        layXPipe.error = null
                        layYPipe.error = null
                        return
                    }
                    (isInputXPipeValid && isInputYPipeValid) && (!isInputXSpiValid && !isInputYSpiValid) -> {
                        listener.onSelect(
                            Const.TAG_SURVEY_PIPE, Const.RESULT_PASS,
                            selectIndex.toString(),
                            inputXPipe.text.toString(),
                            inputYPipe.text.toString(),
                        )
                        dismissAllowingStateLoss()
                    }
                    (!isInputXPipeValid && !isInputYPipeValid) && (isInputXSpiValid && isInputYSpiValid) -> {
                        listener.onSelect(
                            Const.TAG_SURVEY_SPI, Const.RESULT_PASS,
                            selectIndex.toString(),
                            inputXSpi.text.toString(),
                            inputYSpi.text.toString()
                        )
                        dismissAllowingStateLoss()
                    }
                    (isInputXPipeValid && isInputYPipeValid) && (isInputXSpiValid && isInputYSpiValid) -> {
                        listener.onSelect(
                            Const.TAG_SURVEY, Const.RESULT_PASS,
                            selectIndex.toString(),
                            inputXPipe.text.toString(),
                            inputYPipe.text.toString(),
                            inputXSpi.text.toString(),
                            inputYSpi.text.toString()
                        )
                        dismissAllowingStateLoss()
                    }
                    else -> {
                    }
                }
            }
            R.id.button_dismiss -> {
                listener.onSelect(Const.TAG_SURVEY, Const.RESULT_FAIL, null)
                dismissAllowingStateLoss()
            }
        }
    }

    private fun validate(layout: TextInputLayout, input: TextInputEditText): Boolean {
        try {
            if (input.text.toString().toDouble() > 999999.9999) {
                layout.error = getString(R.string.map_coord_error)
            } else {
                layout.error = null
                input.clearFocus()
                return true
            }
        } catch (e: NullPointerException) {
            layout.error = getString(R.string.map_input_error)
            return false
        } catch (e: NumberFormatException) {
            layout.error = getString(R.string.map_input_error)
            return false
        }
        return false
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }
}