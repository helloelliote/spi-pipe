package kr.djspi.pipe01.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_location.*
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.TAG_LOCATION
import kr.djspi.pipe01.R

class LocationDialog : DialogFragment(), OnClickListener {

    private var selectIndex = -1
    private var dialogTitle: String? = null
    private lateinit var listener: OnSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogTitle = getString(R.string.popup_title_location)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location, container, false)
        popup_title.text = dialogTitle
        arrayOf<View>(btn_survey, btn_gps, button_dismiss).forEach {
            it.setOnClickListener(this)
        }
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_survey -> {
                selectIndex = 1
                listener.onSelect(TAG_LOCATION, selectIndex, null)
                dismissAllowingStateLoss()
            }
            R.id.btn_gps -> {
                selectIndex = 2
                listener.onSelect(TAG_LOCATION, selectIndex, null)
                dismissAllowingStateLoss()
            }
            R.id.button_dismiss -> {
                listener.onSelect(TAG_LOCATION, RESULT_FAIL, null)
                dismissAllowingStateLoss()
            }
        }
    }
}
