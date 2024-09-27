package kr.djspi.pipe01.fragment

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.djspi.pipe01.Const.TAG_SHAPE
import kr.djspi.pipe01.R
import kr.djspi.pipe01.dto.PipeShape

class ListDialog : DialogFragment(), OnClickListener {

    private var listTag: String? = null
    private var dialogTitle: String? = null
    private var componentName: String? = null
    private var selectIndex: Int = -1
    private lateinit var listView: RecyclerView
    private lateinit var adapter: MyAdapter
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
//            TAG_PIPE -> {
//                Const.PIPE_TYPE_ENUMS.forEach {
//                    listItem.add(it.name)
//                }
//                dialogTitle = getString(R.string.popup_title_select_pipe)
//            }
            TAG_SHAPE -> {
                listItem.addAll(PipeShape.PipeShapeEnum.getSelectableTypes())
                dialogTitle = getString(R.string.popup_title_select_shape)
            }
//            TAG_SUPERVISE -> {
//                Thread(Runnable {
//                    BaseActivity.superviseDb!!.dao().all.forEach {
//                        listItem.add(it.supervise)
//                    }
//                }).start()
//                dialogTitle = getString(R.string.popup_title_select_supervise)
//            }
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
        listView.layoutManager = LinearLayoutManager(view.context)
        adapter = MyAdapter(listItem)
        { selectedItem ->
            this.componentName = selectedItem
            this.selectIndex = listItem.indexOf(componentName)
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
        super.onPause()
        listView.adapter = null
    }

    override fun onResume() {
        super.onResume()
        listView.adapter = adapter
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectIndex = -1
        super.onDismiss(dialog)
    }
}

class MyAdapter(
    private val dataSet: ArrayList<String>,
    private val onItemSelected: (String) -> Unit,
): RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var selectedItemPosition = -1
    private var selectedLayout: LinearLayout? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemLayout: LinearLayout = view.findViewById(R.id.item_root)
        val itemText: TextView = view.findViewById(R.id.item_text)

        fun bind(item: String) {
            itemText.text = item
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.fragment_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        val layout = viewHolder.itemLayout
        viewHolder.bind(item)

        layout.setOnClickListener {
            val currentPosition = viewHolder.adapterPosition
            if (selectedItemPosition >= 0 || selectedLayout != null) {
                selectedLayout?.setBackgroundColor(Color.TRANSPARENT)
            }

            selectedItemPosition = currentPosition
            selectedLayout = layout
            selectedLayout?.setBackgroundColor(Color.parseColor("#ffccbc"))
            onItemSelected(item)
        }
    }

    override fun getItemCount(): Int = dataSet.size
}
