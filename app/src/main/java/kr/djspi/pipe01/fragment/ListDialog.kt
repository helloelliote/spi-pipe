package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.sylversky.indexablelistview.scroller.Indexer
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list_item.*
import kr.djspi.pipe01.BaseActivity.Companion.superviseDb
import kr.djspi.pipe01.Const.*
import kr.djspi.pipe01.R

class ListDialog : DialogFragment(), View.OnClickListener, OnSelectListener {

    private var listTag: String? = null
    private var dialogTitle: String? = null
    private var componentName: String? = null
    private var selectPosition: Int = -1
    private var selectIndex: Int = -1
    private var state: Parcelable? = null
    private lateinit var listItem: ArrayList<String>
    private lateinit var listener: OnSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listTag = tag
        listItem = ArrayList(0)
        if (context is OnSelectListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (listTag) {
            TAG_PIPE -> {
                PIPE_TYPE_ENUMS.forEach {
                    listItem.add(it.name)
                }
                dialogTitle = getString(R.string.popup_title_select_pipe)
            }
            TAG_SHAPE -> {
                listItem.addAll(listOf(*PIPE_SHAPES))
                dialogTitle = getString(R.string.popup_title_select_shape)
            }
            TAG_SUPERVISE -> {
                Thread {
                    superviseDb!!.dao().all.forEach {
                        listItem.add(it.supervise)
                    }
                }.start()
                dialogTitle = getString(R.string.popup_title_select_supervise)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        btn_ok.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        button_close.setOnClickListener(this)

        if (listTag == TAG_SUPERVISE) {
            list_common.adapter = ListAdapter(context, listItem, true)
        } else {
            list_common.adapter = ListAdapter(context, listItem, false)
            list_common.isFastScrollEnabled = false
        }
        list_common.setOnItemClickListener { _, _, position, _ ->
            componentName = listItem[position]
            selectPosition = position
        }
        state?.let {
            list_common.requestFocus()
            list_common.onRestoreInstanceState(it)
        }
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok -> {
                if (selectIndex == -1) {
                    Toast.makeText(context, "항목을 선택해주세요", Toast.LENGTH_LONG).show()
                    return
                }
                listener.onSelect(listTag, selectIndex, componentName)
                dismissAllowingStateLoss()
            }
            R.id.btn_cancel, R.id.button_close -> dismissAllowingStateLoss()
        }
    }

    override fun onSelect(tag: String?, index: Int, vararg text: String?) {

    }

    override fun onPause() {
        state = list_common.onSaveInstanceState()
        super.onPause()
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }

    inner class ListAdapter(
        val context: Context?,
        private val listItem: ArrayList<String>,
        private val isListSupervise: Boolean
    ) : BaseAdapter(), Indexer {

        private val customSection: CustomSection?

        init {
            if (isListSupervise) customSection = CustomSection(this)
            else customSection = null
        }

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.fragment_list_item, null)
            }
            txt_name.apply {
                text = listItem[position]
                textAlignment =
                    if (isListSupervise) View.TEXT_ALIGNMENT_TEXT_START else TEXT_ALIGNMENT_CENTER
                setOnFocusChangeListener { _, _ -> }
            }
            return view!!
        }

        override fun getItem(position: Int): Any = listItem[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = listItem.size

        override fun getComponentName(position: Int): String = listItem[position]

        override fun getSectionForPosition(position: Int): Int = 0

        override fun getSections(): Array<String?>? {
            return if (isListSupervise) customSection?.arraySections else null
        }

        override fun getPositionForSection(sectionIndex: Int): Int {
            return if (isListSupervise) customSection?.getPositionForSection(
                sectionIndex,
                count
            )!! else 0
        }
    }
}
