package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import kr.djspi.pipe01.BaseActivity.Companion.defPackage
import kr.djspi.pipe01.Const.TAG_DIRECTION
import kr.djspi.pipe01.Const.TAG_POSITION
import kr.djspi.pipe01.R

class PositionDialog : DialogFragment(), OnClickListener {

    private var selectIndex = -1
    private var typeString: String? = null
    private var dialogTitle: String? = null
    private var bundle: Bundle? = null
    private var shapeString: String? = null
    private val selects = arrayOfNulls<ImageView>(10)
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
        }
        dialogTitle = getString(R.string.popup_title_select_position)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plot_position, container, false)
        val title = view.findViewById<TextView>(R.id.popup_title)
        title.text = dialogTitle
        arrayOf<View>(
            view.findViewById(R.id.lay_1),
            view.findViewById(R.id.lay_2),
            view.findViewById(R.id.lay_3),
            view.findViewById(R.id.lay_4),
            view.findViewById(R.id.lay_5),
            view.findViewById(R.id.lay_6),
            view.findViewById(R.id.lay_7),
            view.findViewById(R.id.lay_8),
            view.findViewById(R.id.lay_9),
            view.findViewById(R.id.button_close),
            view.findViewById(R.id.btn_cancel),
            view.findViewById(R.id.btn_ok)
        ).forEach {
            it.setOnClickListener(this)
        }
        setLayoutVisibility(view)
        return view
    }

    private fun setLayoutVisibility(view: View) {
        val defType = "id"
        val views = arrayOfNulls<ImageView>(10)
        (1..9).forEach { i ->
            views[i] = view.findViewById(resources.getIdentifier("image_$i", defType, defPackage))
            selects[i] =
                view.findViewById(resources.getIdentifier("v_select_$i", defType, defPackage))
        }
        val background = view.findViewById<ImageView>(R.id.lay_background)
        when (typeString) {
            TAG_TYPE_PLATE -> {
                background.setImageDrawable(fromRes(R.drawable.bg_p))
                view.findViewById<LinearLayout>(R.id.lay_row_2).visibility = INVISIBLE
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_01_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_01_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_01_9))
                views[7]!!.setImageDrawable(fromRes(R.drawable.btn_01_1))
                views[8]!!.setImageDrawable(fromRes(R.drawable.btn_01_2))
                views[9]!!.setImageDrawable(fromRes(R.drawable.btn_01_3))
            }
            TAG_TYPE_MARKER -> {
                background.setImageDrawable(fromRes(R.drawable.bg_m))
                view.findViewById<LinearLayout>(R.id.lay_row_3).visibility = GONE
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_10_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_10_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_10_9))
                view.findViewById<FrameLayout>(R.id.lay_4).visibility = GONE
                views[5]!!.setImageDrawable(fromRes(R.drawable.btn_10_2))
                view.findViewById<FrameLayout>(R.id.lay_6).visibility = GONE
            }
            TAG_TYPE_COLUMN -> {
                val params = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER)
                params.setMargins(0, 60, 0, 0)
                view.findViewById<LinearLayout>(R.id.lay_rows).layoutParams = params
                background.setImageDrawable(fromRes(R.drawable.bg_c))
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_11_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_11_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_11_9))
                views[4]!!.setImageDrawable(fromRes(R.drawable.btn_11_4))
                views[5]!!.setImageDrawable(fromRes(R.drawable.btn_11_5))
                views[6]!!.setImageDrawable(fromRes(R.drawable.btn_11_6))
                views[7]!!.setImageDrawable(fromRes(R.drawable.btn_11_1))
                views[8]!!.setImageDrawable(fromRes(R.drawable.btn_11_2))
                views[9]!!.setImageDrawable(fromRes(R.drawable.btn_11_3))
                if (shapeString == "직진형") { // 직진형
                    views[1]!!.visibility = GONE
                    (views[1]!!.parent as View).visibility = GONE
                    views[3]!!.visibility = GONE
                    (views[3]!!.parent as View).visibility = GONE
                    views[7]!!.visibility = GONE
                    (views[7]!!.parent as View).visibility = GONE
                    views[9]!!.visibility = GONE
                    (views[9]!!.parent as View).visibility = GONE
                }
            }
        }
    }

    private fun fromRes(@DrawableRes resId: Int): Drawable {
        return resources.getDrawable(resId, null)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> {
                if (selectIndex == -1) {
                    Toast.makeText(context, "관로의 위치를 선택해주세요", Toast.LENGTH_LONG).show()
                    return
                }
                listener.onSelect(TAG_POSITION, selectIndex, null)
                bundle!!.putInt("positionInt", selectIndex)
                DirectionDialog().apply { arguments = bundle }
                    .show(parentFragmentManager, TAG_DIRECTION)
                dismissAllowingStateLoss()
            }
            R.id.lay_1 -> {
                selectIndex = 1
                setFocus(v, selectIndex)
            }
            R.id.lay_2 -> {
                selectIndex = 2
                setFocus(v, selectIndex)
            }
            R.id.lay_3 -> {
                selectIndex = 3
                setFocus(v, selectIndex)
            }
            R.id.lay_4 -> {
                selectIndex = 4
                setFocus(v, selectIndex)
            }
            R.id.lay_5 -> {
                selectIndex = 5
                setFocus(v, selectIndex)
            }
            R.id.lay_6 -> {
                selectIndex = 6
                setFocus(v, selectIndex)
            }
            R.id.lay_7 -> {
                selectIndex = 7
                setFocus(v, selectIndex)
            }
            R.id.lay_8 -> {
                selectIndex = 8
                setFocus(v, selectIndex)
            }
            R.id.lay_9 -> {
                selectIndex = 9
                setFocus(v, selectIndex)
            }
            R.id.btn_cancel, R.id.button_close -> dismissAllowingStateLoss()
        }
    }

    private fun setFocus(view: View, selectIndex: Int) {
        selects.forEach {
            it?.visibility = INVISIBLE
        }
        selects[selectIndex]?.visibility = VISIBLE
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }

    companion object {
        private const val TAG_TYPE_PLATE = "표지판"
        private const val TAG_TYPE_MARKER = "표지기"
        private const val TAG_TYPE_COLUMN = "표지주"
    }
}
