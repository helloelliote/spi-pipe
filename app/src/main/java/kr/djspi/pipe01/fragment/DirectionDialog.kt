package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_plot_direction.*
import kr.djspi.pipe01.BaseActivity.Companion.defPackage
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS
import kr.djspi.pipe01.Const.TAG_DIRECTION
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.Companion.parsePipeShape
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType
import kr.djspi.pipe01.util.show

class DirectionDialog : DialogFragment(), OnClickListener {

    private var selectIndex = -1
    private var positionInt = -1
    private var dialogTitle: String? = null
    private var typeString: String? = null
    private var shapeString: String? = null
    private var bundle: Bundle? = null
    private lateinit var resIds: ArrayList<String>
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
            bundle = it
            typeString = it.getString("typeString")
            shapeString = it.getString("shapeString")
            positionInt = it.getInt("positionInt")
        }
        dialogTitle = getString(R.string.popup_title_select_direction)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plot_direction, container, false)
        popup_title.text = dialogTitle
        arrayOf<View>(lay_2, lay_4, lay_6, lay_8, button_close, btn_cancel, btn_ok).forEach {
            it.setOnClickListener(this)
        }
        setLayoutVisibility()
        return view
    }

    private fun setLayoutVisibility() {
        resIds = ArrayList()
        for (i in 1..4) {
            resIds[i * 2] =
                "plan_${parseSpiType(typeString)}_${parsePipeShape(shapeString)}_${positionInt}_${PIPE_DIRECTIONS[i * 2]}"
        }
        setImageView(image_2, resIds[2])
        setImageView(image_4, resIds[4])
        setImageView(image_6, resIds[6])
        setImageView(image_8, resIds[8])
    }

    private fun setImageView(view: ImageView, resId: String) {
        val i = resources.getIdentifier(resId, "drawable", defPackage)
        when {
            i != 0 -> view.setBackgroundResource(i)
            else -> {
                val layout = view.parent as FrameLayout
                layout.visibility = GONE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> {
                if (selectIndex == -1) {
                    Toast.makeText(context, "관로의 방향을 선택해주세요", Toast.LENGTH_LONG).show()
                    return
                }
                listener.onSelect(TAG_DIRECTION, selectIndex, resIds[selectIndex])
                if (positionInt == 5) {
                    dismissAllowingStateLoss()
                    return
                } else {
                    bundle!!.putString("planString", resIds[selectIndex])
                    DistanceDialog().show(TAG_DISTANCE, bundle)
                    dismissAllowingStateLoss()
                }
            }
            R.id.btn_cancel -> {
                listener.onSelect(TAG_DIRECTION, -2, null)
                dismissAllowingStateLoss()
            }
            R.id.button_close -> dismissAllowingStateLoss()
            R.id.lay_2 -> {
                selectIndex = 2
                setFocus(v)
            }
            R.id.lay_8 -> {
                selectIndex = 8
                setFocus(v)
            }
            R.id.lay_4 -> {
                selectIndex = 4
                setFocus(v)
            }
            R.id.lay_6 -> {
                selectIndex = 6
                setFocus(v)
            }
        }
    }

    private fun setFocus(view: View) {
        v_select.visibility = INVISIBLE
        view.findViewById<View>(R.id.v_select).apply {
            this.visibility = VISIBLE
            this.findViewById<View>(R.id.v_select)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }
}
