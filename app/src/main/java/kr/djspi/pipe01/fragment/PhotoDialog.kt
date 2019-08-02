package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.R

class PhotoDialog : DialogFragment(), View.OnClickListener {

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
        dialogTitle = getString(R.string.popup_title_select_photo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)
        val title = view.findViewById<TextView>(R.id.popup_title)
        title.text = dialogTitle
        arrayOf<View>(
            view.findViewById(R.id.button_close),
            view.findViewById(R.id.btn_camera),
            view.findViewById(R.id.btn_gallery)
        ).forEach {
            it.setOnClickListener(this)
        }
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_camera -> {
                selectIndex = 1
                listener.onSelect(TAG_PHOTO, selectIndex, null)
                dismissAllowingStateLoss()
            }
            R.id.btn_gallery -> {
                selectIndex = 2
                listener.onSelect(TAG_PHOTO, selectIndex, null)
                dismissAllowingStateLoss()
            }
            R.id.button_close -> dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }
}
