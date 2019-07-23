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
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_plot_position.*
import kr.djspi.pipe01.BaseActivity.Companion.defPackage
import kr.djspi.pipe01.Const.*
import kr.djspi.pipe01.R
import kr.djspi.pipe01.util.show

class PositionDialog : DialogFragment(), OnClickListener {

    private var selectIndex = -1
    private var typeString: String? = null
    private var dialogTitle: String? = null
    private var bundle: Bundle? = null
    private var shapeString: String? = null
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
        popup_title.text = dialogTitle
        arrayOf<View>(
            lay_1,
            lay_2,
            lay_3,
            lay_4,
            lay_5,
            lay_6,
            lay_7,
            lay_8,
            lay_9,
            button_close,
            btn_cancel,
            btn_ok
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
        }
        when (typeString) {
            TAG_TYPE_PLATE -> {
                lay_background.setImageDrawable(fromRes(R.drawable.bg_p))
                lay_row_2.visibility = INVISIBLE
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_01_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_01_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_01_9))
                views[7]!!.setImageDrawable(fromRes(R.drawable.btn_01_1))
                views[8]!!.setImageDrawable(fromRes(R.drawable.btn_01_2))
                views[9]!!.setImageDrawable(fromRes(R.drawable.btn_01_3))
            }
            TAG_TYPE_MARKER -> {
                lay_background.setImageDrawable(fromRes(R.drawable.bg_m))
                lay_row_3.visibility = GONE
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_10_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_10_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_10_9))
                views[5]!!.setImageDrawable(fromRes(R.drawable.btn_10_2))
            }
            TAG_TYPE_COLUMN -> {
                val params = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER)
                params.setMargins(0, 60, 0, 0)
                lay_rows.layoutParams = params
                lay_background.setImageDrawable(fromRes(R.drawable.bg_c))
                views[1]!!.setImageDrawable(fromRes(R.drawable.btn_11_7))
                views[2]!!.setImageDrawable(fromRes(R.drawable.btn_11_8))
                views[3]!!.setImageDrawable(fromRes(R.drawable.btn_11_9))
                views[4]!!.setImageDrawable(fromRes(R.drawable.btn_11_4))
                views[5]!!.setImageDrawable(fromRes(R.drawable.btn_11_5))
                views[6]!!.setImageDrawable(fromRes(R.drawable.btn_11_6))
                views[7]!!.setImageDrawable(fromRes(R.drawable.btn_11_1))
                views[8]!!.setImageDrawable(fromRes(R.drawable.btn_11_2))
                views[9]!!.setImageDrawable(fromRes(R.drawable.btn_11_3))
                if (shapeString == PIPE_SHAPES[0]) { // 직진형
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
                DirectionDialog().show(TAG_DIRECTION, bundle)
                dismissAllowingStateLoss()
            }
            R.id.lay_1 -> {
                selectIndex = 1
                setFocus(v)
            }
            R.id.lay_2 -> {
                selectIndex = 2
                setFocus(v)
            }
            R.id.lay_3 -> {
                selectIndex = 3
                setFocus(v)
            }
            R.id.lay_4 -> {
                selectIndex = 4
                setFocus(v)
            }
            R.id.lay_5 -> {
                selectIndex = 5
                setFocus(v)
            }
            R.id.lay_6 -> {
                selectIndex = 6
                setFocus(v)
            }
            R.id.lay_7 -> {
                selectIndex = 7
                setFocus(v)
            }
            R.id.lay_8 -> {
                selectIndex = 8
                setFocus(v)
            }
            R.id.lay_9 -> {
                selectIndex = 9
                setFocus(v)
            }
            R.id.btn_cancel, R.id.button_close -> dismissAllowingStateLoss()
        }
    }

    private fun setFocus(view: View) {
        v_select.visibility = INVISIBLE
        view.findViewById<View>(R.id.v_select).apply {
            this.visibility = VISIBLE
            this.findViewById(R.id.v_select)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }

    companion object {
        private val TAG = PositionDialog::class.java.simpleName
        private const val TAG_TYPE_PLATE = "표지판"
        private const val TAG_TYPE_MARKER = "표지기"
        private const val TAG_TYPE_COLUMN = "표지주"
    }
}
