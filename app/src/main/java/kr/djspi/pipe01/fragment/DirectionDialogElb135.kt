package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.DialogFragment
import kr.djspi.pipe01.BaseActivity.Companion.defPackage
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS_ELB135
import kr.djspi.pipe01.Const.TAG_DIRECTION_ELB135
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.Companion.parsePipeShape
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType

class DirectionDialogElb135 : DialogFragment(), OnClickListener {

    private var selectIndex = -1
    private var positionInt = -1
    private var dialogTitle: String? = null
    private var typeString: String? = null
    private var shapeString: String? = null
    private var bundle: Bundle? = null
    private var resIds = arrayOfNulls<String>(10)
    private val selects = arrayOfNulls<ImageView>(10)
    private lateinit var dialogTitleSub: String
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
        val view = inflater.inflate(R.layout.fragment_plot_direction_2, container, false)
        val title = view.findViewById<TextView>(R.id.popup_title)
        val titleSub = view.findViewById<TextView>(R.id.popup_title_sub)
        title.text = dialogTitle
        dialogTitleSub = getString(R.string.popup_title_select_direction_sub)
        titleSub.text = dialogTitleSub
        arrayOf<View>(
            view.findViewById(R.id.lay_1),
            view.findViewById(R.id.lay_2),
            view.findViewById(R.id.lay_3),
            view.findViewById(R.id.lay_4),
            view.findViewById(R.id.lay_5),
            view.findViewById(R.id.lay_6),
            view.findViewById(R.id.lay_7),
            view.findViewById(R.id.lay_8),
            view.findViewById(R.id.button_close),
            view.findViewById(R.id.btn_cancel),
            view.findViewById(R.id.btn_ok)
        ).forEach {
            it.setOnClickListener(this)
        }
        setLayoutVisibility(view)
        val animationNext = AnimationUtils.loadAnimation(context, R.anim.shake_next)
        val animationPrev = AnimationUtils.loadAnimation(context, R.anim.shake_prev)
        view.findViewById<ImageButton>(R.id.imageButtonNext).startAnimation(animationNext)
        view.findViewById<ImageButton>(R.id.imageButtonPrev).startAnimation(animationPrev)
        return view
    }

    private fun setLayoutVisibility(view: View) {
        val defType = "id"
        for (i in 1..8) {
            resIds[i] =
                "plan_${parseSpiType(typeString)}_${parsePipeShape(shapeString)}_${positionInt}_${PIPE_DIRECTIONS_ELB135[i]}"
            selects[i] =
                view.findViewById(resources.getIdentifier("v_select_${i}", defType, defPackage))
        }
        setImageView(view.findViewById(R.id.image_1), resIds[1]!!)
        setImageView(view.findViewById(R.id.image_2), resIds[2]!!)
        setImageView(view.findViewById(R.id.image_3), resIds[3]!!)
        setImageView(view.findViewById(R.id.image_4), resIds[4]!!)
        setImageView(view.findViewById(R.id.image_5), resIds[5]!!)
        setImageView(view.findViewById(R.id.image_6), resIds[6]!!)
        setImageView(view.findViewById(R.id.image_7), resIds[7]!!)
        setImageView(view.findViewById(R.id.image_8), resIds[8]!!)
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
                    Toast.makeText(context, dialogTitleSub, Toast.LENGTH_LONG).show()
                    return
                }
                listener.onSelect(TAG_DIRECTION_ELB135, selectIndex, resIds[selectIndex])
                if (positionInt == 5) {
                    dismissAllowingStateLoss()
                    return
                } else {
                    bundle!!.putString("planString", resIds[selectIndex])
                    DistanceDialog().apply { arguments = bundle }
                        .show(parentFragmentManager, TAG_DISTANCE)
                    dismissAllowingStateLoss()
                }
            }
            R.id.btn_cancel -> {
                listener.onSelect(TAG_DIRECTION_ELB135, -2, null)
                dismissAllowingStateLoss()
            }
            R.id.button_close -> dismissAllowingStateLoss()
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
}
