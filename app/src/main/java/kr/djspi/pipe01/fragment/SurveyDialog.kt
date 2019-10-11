package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_SURVEY
import kr.djspi.pipe01.R
import kr.djspi.pipe01.geolocation.GeoTrans
import kr.djspi.pipe01.geolocation.GeoTrans.Coordinate.*
import kr.djspi.pipe01.util.DecimalFilter

class SurveyDialog : DialogFragment(), View.OnClickListener {

    private var dialogTitle: String? = null
    private var selectIndex: Int = -1
    private var checkeId: Int = -1
    private lateinit var inputX: TextInputEditText
    private lateinit var inputY: TextInputEditText
    private lateinit var layX: TextInputLayout
    private lateinit var layY: TextInputLayout
    private lateinit var listener: OnSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogTitle = getString(R.string.popup_title_survey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_survey, container, false)
        val popupTitle = view.findViewById<TextView>(R.id.popup_title)
        popupTitle.text = dialogTitle
        val nmapRadiogroup = view.findViewById<RadioGroup>(R.id.nmap_radioGroup)
        nmapRadiogroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            checkeId = checkedId
            selectIndex = group.indexOfChild(checkedRadioButton)
            when (checkedId) {
                R.id.nmap_radio_central -> originPoint = GRS80_MIDDLE_WITH_JEJUDO
                R.id.nmap_radio_east -> originPoint = GRS80_EAST
                R.id.nmap_radio_eastsea -> originPoint = GRS80_EASTSEA
                R.id.nmap_radio_west -> originPoint = GRS80_WEST
            }
        }
        inputX = view.findViewById(R.id.input_x)
        inputY = view.findViewById(R.id.input_y)
        inputX.filters = arrayOf(DecimalFilter(10, 4))
        inputY.filters = arrayOf(DecimalFilter(10, 4))
        layX = view.findViewById(R.id.lay_x)
        layY = view.findViewById(R.id.lay_y)
        arrayOf<View>(
            view.findViewById(R.id.button_dismiss),
            view.findViewById(R.id.btn_ok)
        ).forEach {
            it.setOnClickListener(this)
        }
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> {
                if (checkeId == -1) {
                    layX.error = "원점을 선택해주세요."
                    layY.error = "원점을 선택해주세요."
                    return
                }
                if (isInputValid()) {
                    listener.onSelect(
                        TAG_SURVEY, RESULT_PASS,
                        inputX.text.toString(),
                        inputY.text.toString()
                    )
                    dismiss()
                } else selectIndex = -1
            }
            R.id.button_dismiss -> {
                listener.onSelect(TAG_SURVEY, RESULT_FAIL, null)
                dismissAllowingStateLoss()
            }
        }
    }

    /**
     * 사용자의 입력값이 유효한 값인지 검사: Null 체크, 유효범위 체크
     *
     * @return boolean isX, boolean isY 입력값 X AND Y 가 유효하면 true 리턴
     */
    private fun isInputValid(): Boolean {
        var isX = false
        var isY = false

        try {
            if (inputX.text.toString().toDouble() > 999999.9999) {
                layX.error = getString(R.string.map_coord_error)
            } else {
                isX = true
                layX.error = null
                inputX.clearFocus()
            }
        } catch (e: NullPointerException) {
            layX.error = getString(R.string.map_input_error)
        } catch (e: NumberFormatException) {
            layX.error = getString(R.string.map_input_error)
        }

        try {
            if (inputY.text.toString().toDouble() > 999999.9999) {
                layY.error = getString(R.string.map_coord_error)
            } else {
                isY = true
                layY.error = null
                inputY.clearFocus()
            }
        } catch (e: NullPointerException) {
            layY.error = getString(R.string.map_input_error)
        } catch (e: NumberFormatException) {
            layY.error = getString(R.string.map_input_error)
        }
        return isX && isY
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        checkeId = -1
        super.onDismiss(dialog)
    }

    companion object {
        lateinit var originPoint: GeoTrans.Coordinate
    }
}
