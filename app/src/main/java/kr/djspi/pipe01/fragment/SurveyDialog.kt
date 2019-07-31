package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_location_survey.*
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
        popup_title.text = dialogTitle
        nmap_radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            selectIndex = group.indexOfChild(checkedRadioButton)
            when (checkedId) {
                R.id.nmap_radio_central -> originPoint = GRS80_MIDDLE_WITH_JEJUDO
                R.id.nmap_radio_east -> originPoint = GRS80_EAST
                R.id.nmap_radio_eastsea -> originPoint = GRS80_EASTSEA
                R.id.nmap_radio_west -> originPoint = GRS80_WEST
            }
        }
        input_x.filters = arrayOf(DecimalFilter(4, 2))
        input_y.filters = arrayOf(DecimalFilter(4, 2))
        button_dismiss.setOnClickListener(this)
        btn_ok.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> {
                if (isInputValid()) {
                    listener.onSelect(
                        TAG_SURVEY, RESULT_PASS,
                        input_x.text.toString(),
                        input_y.text.toString()
                    )
                    dismissAllowingStateLoss()
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
            if (input_x.text.toString().toDouble() > 999999.9999) {
                lay_x.error = getString(R.string.map_coord_error)
            } else {
                isX = true
                lay_x.error = null
                input_x.clearFocus()
            }
        } catch (e: NullPointerException) {
            lay_x.error = getString(R.string.map_input_error)
        } catch (e: NumberFormatException) {
            lay_x.error = getString(R.string.map_input_error)
        }

        try {
            if (input_y.text.toString().toDouble() > 999999.9999) {
                lay_y.error = getString(R.string.map_coord_error)
            } else {
                isY = true
                lay_y.error = null
                input_y.clearFocus()
            }
        } catch (e: NullPointerException) {
            lay_y.error = getString(R.string.map_input_error)
        } catch (e: NumberFormatException) {
            lay_y.error = getString(R.string.map_input_error)
        }
        return isX && isY
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }

    companion object {
        lateinit var originPoint: GeoTrans.Coordinate
    }
}
