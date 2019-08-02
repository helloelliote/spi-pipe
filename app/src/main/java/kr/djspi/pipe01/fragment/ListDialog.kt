package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.sylversky.indexablelistview.scroller.Indexer
import com.sylversky.indexablelistview.widget.IndexableListView
import kr.djspi.pipe01.BaseActivity.Companion.superviseDb
import kr.djspi.pipe01.Const.PIPE_SHAPES
import kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS
import kr.djspi.pipe01.Const.TAG_PIPE
import kr.djspi.pipe01.Const.TAG_SHAPE
import kr.djspi.pipe01.Const.TAG_SUPERVISE
import kr.djspi.pipe01.R

class ListDialog : DialogFragment(), OnClickListener {

    private var listTag: String? = null
    private var dialogTitle: String? = null
    private var componentName: String? = null
    private var selectIndex: Int = -1
    private var state: Parcelable? = null
    private lateinit var listView: IndexableListView
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
                    superviseDb?.close()
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
        val titleView = view.findViewById<TextView>(R.id.popup_list_title)
        titleView.text = dialogTitle
        listView = view.findViewById(R.id.list_common)
        if (listTag == TAG_SUPERVISE) {
            listView.adapter = ListAdapter(context, listItem, true)
        } else {
            listView.adapter = ListAdapter(context, listItem, false)
            listView.isFastScrollEnabled = false
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            componentName = listItem[position]
            selectIndex = position
        }
        state?.let {
            listView.requestFocus()
            listView.onRestoreInstanceState(it)
        }
        view.findViewById<TextView>(R.id.btn_ok).setOnClickListener(this)
        view.findViewById<TextView>(R.id.btn_cancel).setOnClickListener(this)
        view.findViewById<ImageView>(R.id.button_close).setOnClickListener(this)

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

    override fun onPause() {
        state = listView.onSaveInstanceState()
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
            val textView = view!!.findViewById<TextView>(R.id.txt_name)
            textView.apply {
                text = listItem[position]
                textAlignment =
                    if (isListSupervise) View.TEXT_ALIGNMENT_TEXT_START else TEXT_ALIGNMENT_CENTER
                setOnFocusChangeListener { _, _ -> }
            }
            return view
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
