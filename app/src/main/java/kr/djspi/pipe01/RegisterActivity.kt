package kr.djspi.pipe01

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.FileProvider
import com.andreabaccega.widget.FormEditText
import com.helloelliote.util.image.ImageUtil
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_register.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.Const.*
import kr.djspi.pipe01.dto.*
import kr.djspi.pipe01.fragment.*
import kr.djspi.pipe01.util.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.lang.String.format

class RegisterActivity : BaseActivity(), OnSelectListener, View.OnClickListener, Serializable {

    private lateinit var spi: Spi
    private lateinit var spiType: SpiType
    private lateinit var spiMemo: SpiMemo
    private lateinit var spiPhoto: SpiPhoto
    private lateinit var spiLocation: SpiLocation
    private lateinit var imm: InputMethodManager
    private val pipe: Pipe = Pipe()
    private val pipeType = PipeType()
    private val pipeShape = PipeShape()
    private val pipePosition = PipePosition()
    private val pipePlan = PipePlan()
    private val pipeSupervise = PipeSupervise()
    private var photoObj: SpiPhotoObject? = null
    private var tempFile: File? = null
    private var tempUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let {
            val serializable = it.getSerializableExtra("RegisterActivity")
            if (serializable is HashMap<*, *>) {
                spi = serializable["Spi"] as Spi
                spiType = serializable["SpiType"] as SpiType
                spiLocation = serializable["SpiLocation"] as SpiLocation
                spiMemo = serializable["SpiMemo"] as SpiMemo
                spiPhoto = serializable["SpiPhoto"] as SpiPhoto
            }
        }
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setContentView(R.layout.activity_register)
        restoreInstanceState()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar.title = "SPI 매설관로 ${spiType.type}"

        setOnClickListeners()
        form_shape.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                pipeShape.shape = s.toString()
            }
        })
        form_horizontal.isFocusable = false
        form_vertical.isFocusable = false
        form_depth.filters = arrayOf(DecimalFilter(4, 2))
        form_supervise_contact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        form_material.setOnEditorActionListener { v, actionId, _ ->
            var isHandled = false
            if (actionId == IME_ACTION_NEXT && form_supervise.text.toString() == "") {
                imm.toggleSoftInputFromWindow(v.windowToken, 0, 0)
                ListDialog().show(this@RegisterActivity.supportFragmentManager, "supervise")
                isHandled = true
            }
            return@setOnEditorActionListener isHandled
        }
        form_construction_contact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        lay_photo_desc.visibility = View.GONE
        form_photo_name.isFocusable = false
        button_confirm.setOnClickListener(OnNextButtonClick())
    }

    private fun setOnClickListeners() {
        arrayOf(
            lay_pipe,
            form_pipe,
            lay_shape,
            form_shape,
            lay_distance,
            form_horizontal,
            form_vertical,
            lay_supervise,
            form_supervise,
            lay_photo,
            form_photo,
            lay_photo_desc,
            form_photo_thumbnail,
            btn_delete
        ).forEach {
            it.setOnClickListener(this)
        }
    }

    private fun restoreInstanceState() {
        val pref = AppPreference.defaultPrefs(this)
        if (pref["switch_preset", false]!!) {
            runOnUiThread {
                if (pref["pipe_type_id", -1]!! >= 0) {
                    onPipeTypeSelect(pref["pipe_type_id", -1]!!)
                }
                form_material.text = pref["material", null]
                if (pref["supervise", null] != null) {
                    Thread(Runnable {
                        val id =
                            superviseDb!!.dao().selectBySupervise(pref["supervise", null] as String)
                        pipeSupervise.id = id
                        pipe.supervise_id = id
                    }).start()
                    form_supervise.text = pref["supervise", null]
                }
                form_supervise_contact.text = pref["supervise_contact", null]
                form_construction.text = pref["construction", null]
                form_construction_contact.text = pref["construction_contact", null]
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lay_pipe, R.id.form_pipe -> ListDialog().show(TAG_PIPE)
            R.id.lay_shape, R.id.form_shape -> {
                pipeShape.shape = null
                ListDialog().show(TAG_SHAPE)
            }
            R.id.lay_supervise, R.id.form_supervise -> ListDialog().show(TAG_SUPERVISE)
            R.id.lay_distance, R.id.form_horizontal, R.id.form_vertical -> {
                form_horizontal.text = null
                form_vertical.text = null
                if (pipeShape.shape == null) {
                    ListDialog().show(TAG_SHAPE)
                } else {
                    showPositionDialog()
                }
            }
            R.id.lay_photo, R.id.form_photo -> {
                lay_photo_desc.visibility = View.VISIBLE
                PhotoDialog().show(TAG_PHOTO)
            }
            R.id.form_photo_thumbnail -> {
                if (photoObj == null) return
                val bundle = Bundle()
                bundle.putSerializable("SpiPhotoObject", photoObj)
                ImageDialog().show(TAG_PHOTO, bundle)
            }
            R.id.btn_delete -> {
                if (photoObj != null) return
                photoObj!!.uri = null
                photoObj!!.file?.delete()
                photoObj!!.file = null
                tempUri = null
                tempFile?.delete()
                tempFile = null
                form_photo_thumbnail.setImageDrawable(null)
                form_photo_name.apply {
                    isFocusable = false
                    setText(getString(R.string.record_input_photo_delete))
                    setTextColor(resources.getColor(R.color.colorAccent, null))
                }
                form_photo.text = null
            }
        }
    }

    private fun showPositionDialog() {
        val bundle = Bundle()
        bundle.putString("typeString", spiType.type)
        bundle.putString("shapeString", pipeShape.shape)
        PositionDialog().show(TAG_POSITION, bundle)
    }

    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        if (index == -1) return
        when (tag) {
            TAG_PIPE -> {
                onPipeTypeSelect(index)
                if (form_shape.text.toString() == "") {
                    pipeShape.shape = null
                    ListDialog().show(TAG_SHAPE)
                }
            }
            TAG_SHAPE -> {
                form_shape.setText(PipeShape.PipeShapeEnum.values()[index].name)
                form_horizontal.text = null
                form_vertical.text = null
                showPositionDialog()
            }
            TAG_SUPERVISE -> {
                Thread(Runnable {
                    val id = superviseDb!!.dao().selectBySupervise(text[0])
                    pipeSupervise.id = id
                    pipe.supervise_id = id
                }).start()
                runOnUiThread {
                    form_supervise.setText(text[0])
                    form_supervise_contact.requestFocus()
                }
            }
            TAG_POSITION -> {
                pipePosition.position = index
                when (index) {
                    1 -> {
                        form_horizontal.tag = "좌측"
                        form_vertical.tag = "전면"
                    }
                    2 -> {
                        form_horizontal.tag = ""
                        form_vertical.tag = "전면"
                    }
                    3 -> {
                        form_horizontal.tag = "우측"
                        form_vertical.tag = "전면"
                    }
                    4 -> {
                        form_horizontal.tag = "좌측"
                        form_vertical.tag = ""
                    }
                    5 -> {
                        form_horizontal.tag = "직상"
                        form_vertical.tag = "직상"
                        form_horizontal.setText("0.0")
                        form_vertical.setText("0.0")
                        pipePosition.horizontal = 0.0
                        pipePosition.vertical = 0.0
                    }
                    6 -> {
                        form_horizontal.tag = "우측"
                        form_vertical.tag = ""
                    }
                    7 -> {
                        form_horizontal.tag = "좌측"
                        form_vertical.tag = "후면"
                    }
                    8 -> {
                        form_horizontal.tag = ""
                        form_vertical.tag = "후면"
                    }
                    9 -> {
                        form_horizontal.tag = "우측"
                        form_vertical.tag = "후면"
                    }
                    else -> {
                        form_horizontal.tag = "수평"
                        form_vertical.tag = "수직"
                    }
                }
            }
            TAG_DIRECTION -> {
                if (index == -2) { // 사용자가 이전 다이얼로그에서 '취소' 선택
                    showPositionDialog()
                    return
                }
                pipePosition.direction = PIPE_DIRECTIONS[index]
                pipePlan.file_plane = "${text[0]}.png"
            }
            TAG_DISTANCE -> {
                if (index == -2) { // 사용자가 이전 다이얼로그에서 '취소' 선택
                    showPositionDialog()
                    return
                }
                form_horizontal.setText(
                    format(
                        "%string %string",
                        form_horizontal.tag.toString(),
                        text[0]
                    )
                )
                form_vertical.setText(
                    format(
                        "%string %string",
                        form_vertical.tag.toString(),
                        text[1]
                    )
                )
                pipePosition.horizontal = text[0]!!.toDouble()
                pipePosition.vertical = text[1]!!.toDouble()
                form_depth.requestFocus()
                imm.toggleSoftInput(SHOW_IMPLICIT, HIDE_NOT_ALWAYS)
            }
            TAG_PHOTO -> {
                when (index) {
                    1 -> {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (cameraIntent.resolveActivity(packageManager) != null) {
                            try {
                                tempFile = ImageUtil.prepareFile()
                                tempUri = FileProvider.getUriForFile(this, packageName, tempFile!!)
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
                            } catch (e: IOException) {
                                toast(getString(R.string.record_photo_error))
                                return
                            }
                        }
                        startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE)
                    }
                    2 -> {
                        val galleryIntent = Intent(Intent.ACTION_PICK)
                        galleryIntent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*")
                        startActivityForResult(galleryIntent, REQUEST_GALLERY)
                    }
                }
            }
        }
    }

    private fun onPipeTypeSelect(index: Int) {
        val fSpec = findViewById<FormEditText>(R.id.form_spec)
        form_pipe.setText(PIPE_TYPE_ENUMS[index].name)
        header.text = format("%string  ", PIPE_TYPE_ENUMS[index])
        fSpec.hint = format("%string 입력", header).replace("관경", "관로관경")
        fSpec.text = null
        unit.text = format("  %string", PIPE_TYPE_ENUMS[index].unit)
        pipe.type_id = index + 1
        pipeType.id = index + 1
        pipeType.header = PIPE_TYPE_ENUMS[index].header
        pipeType.unit = PIPE_TYPE_ENUMS[index].unit
        if (PIPE_TYPE_ENUMS[index].unit == "mm") fSpec.inputType = TYPE_CLASS_NUMBER
        else {
            fSpec.inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS
            fSpec.error = null
        }
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentIgnore()
    }

    private inner class OnNextButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}