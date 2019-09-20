package kr.djspi.pipe01

import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import com.andreabaccega.widget.FormEditText
import com.bumptech.glide.Glide
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_register.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS
import kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS
import kr.djspi.pipe01.Const.REQUEST_CAPTURE_IMAGE
import kr.djspi.pipe01.Const.REQUEST_GALLERY
import kr.djspi.pipe01.Const.TAG_DIRECTION
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.Const.TAG_PIPE
import kr.djspi.pipe01.Const.TAG_POSITION
import kr.djspi.pipe01.Const.TAG_SHAPE
import kr.djspi.pipe01.Const.TAG_SUPERVISE
import kr.djspi.pipe01.dto.*
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType
import kr.djspi.pipe01.fragment.*
import kr.djspi.pipe01.util.*

class RegisterActivity : BaseActivity(), OnSelectListener, View.OnClickListener, Serializable {

    private lateinit var spi: Spi
    private lateinit var spiType: SpiType
    private lateinit var spiMemo: SpiMemo
    private lateinit var spiPhoto: SpiPhoto
    private lateinit var spiLocation: SpiLocation
    private lateinit var imm: InputMethodManager
    private var photoObj: SpiPhotoObject? = null
    private var tempFile: File? = null
    private var tempUri: Uri? = null
    private val pipe: Pipe = Pipe()
    private val pipeType = PipeType()
    private val pipeShape = PipeShape()
    private val pipePosition = PipePosition()
    private val pipePlan = PipePlan()
    private val pipeSupervise = PipeSupervise()
    private lateinit var lPhotoDesc: LinearLayout
    private lateinit var imageThumb: ImageView
    private lateinit var fPhoto: FormEditText

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
            override fun afterTextChanged(s: Editable?) {
                pipeShape.shape = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
//        form_spec.addTextChangedListener(object: TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                var s: String? = null
//                try {
//                     s= String.format("%,d", parseLong(s.toString()))
//                } catch (ignore: NumberFormatException) {
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//        })
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
            lay_photo_desc
        ).forEach {
            it.setOnClickListener(this)
        }

        fPhoto = findViewById(R.id.form_photo)
        fPhoto.setOnClickListener(this)
        lPhotoDesc = findViewById(R.id.lay_photo_desc)
        lPhotoDesc.setOnClickListener(this)
        lPhotoDesc.visibility = View.GONE
        imageThumb = lPhotoDesc.findViewById(R.id.form_photo_thumbnail)
        imageThumb.setOnClickListener(this)
        val fPhotoName = lPhotoDesc.findViewById<FormEditText>(R.id.form_photo_name)
        fPhotoName.isFocusable = false
        val buttonDelete = lPhotoDesc.findViewById<ImageView>(R.id.btn_delete)
        buttonDelete.setOnClickListener(this)
        button_next.setOnClickListener(OnNextButtonClick())
    }

    private fun restoreInstanceState() {
        val pref = AppPreference.defaultPrefs(this)
        if (pref["switch_preset", false]!!) {
            runOnUiThread {
                if (pref["pipe_type_id", -1]!! >= 0) {
                    onPipeTypeSelect(pref["pipe_type_id", -1]!!)
                }
                form_material.setText(pref["material", ""])
                Thread(Runnable {
                    val id =
                        superviseDb!!.dao().selectBySupervise(pref["supervise", ""])
                    pipeSupervise.id = id
                    pipe.supervise_id = id
                    superviseDb?.close()
                }).start()
                form_supervise.setText(pref["supervise", ""])
                form_supervise_contact.setText(pref["supervise_contact", ""])
                form_construction.setText(pref["construction", ""])
                form_construction_contact.setText(pref["construction_contact", ""])
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lay_pipe, R.id.form_pipe -> ListDialog().show(supportFragmentManager, TAG_PIPE)
            R.id.lay_shape, R.id.form_shape -> {
                pipeShape.shape = null
                ListDialog().show(supportFragmentManager, TAG_SHAPE)
            }
            R.id.lay_supervise, R.id.form_supervise -> ListDialog().show(
                supportFragmentManager,
                TAG_SUPERVISE
            )
            R.id.lay_distance, R.id.form_horizontal, R.id.form_vertical -> {
                form_horizontal.text = null
                form_vertical.text = null
                if (pipeShape.shape == null) {
                    ListDialog().show(supportFragmentManager, TAG_SHAPE)
                } else {
                    showPositionDialog()
                }
            }
            R.id.lay_photo, R.id.form_photo -> {
                lay_photo_desc.visibility = View.VISIBLE
                PhotoDialog().show(supportFragmentManager, TAG_PHOTO)
            }
            R.id.form_photo_thumbnail -> {
                photoObj?.let {
                    val bundle = Bundle()
                    bundle.putSerializable("SpiPhotoObject", it)
                    ImageDialog().apply {
                        arguments = bundle
                    }.show(supportFragmentManager, TAG_PHOTO)
                }
            }
            R.id.btn_delete -> {
                photoObj?.let {
                    photoObj?.uri = null
                    photoObj?.file?.delete()
                    photoObj?.file = null
                    tempUri = null
                    tempFile?.delete()
                    tempFile = null
                    form_photo_thumbnail.setImageDrawable(null)
                    form_photo_name.apply {
                        isFocusable = false
                        setText(getString(R.string.record_input_photo_delete))
                        setTextColor(resources.getColor(R.color.colorAccent))
                    }
                    form_photo.text = null
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        if (index == -1) return
        when (tag) {
            TAG_PIPE -> {
                onPipeTypeSelect(index)
                if (form_shape.text.toString() == "") {
                    pipeShape.shape = null
                    ListDialog().show(supportFragmentManager, TAG_SHAPE)
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
                    superviseDb?.close()
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
                form_horizontal.setText("${form_horizontal.tag} ${text[0]}")
                form_vertical.setText("${form_vertical.tag} ${text[0]}")
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
                                tempFile = ImageUtil.prepareFile(this)
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

    private fun showPositionDialog() {
        val bundle = Bundle()
        bundle.putString("typeString", spiType.type)
        bundle.putString("shapeString", pipeShape.shape)
        PositionDialog().apply { arguments = bundle }.show(supportFragmentManager, TAG_POSITION)
    }

    @SuppressLint("SetTextI18n")
    private fun onPipeTypeSelect(index: Int) {
        form_pipe.setText(PIPE_TYPE_ENUMS[index].name)
        header.text = PIPE_TYPE_ENUMS[index].header
        val fSpec = findViewById<FormEditText>(R.id.form_spec)
//        fSpec.hint = "$header 입력".replace("관경", "관로관경")
        fSpec.text = null
        unit.text = "  ${PIPE_TYPE_ENUMS[index].unit}"
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            val resizeFile: File?
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    Glide.with(this).load(tempUri).into(form_photo_thumbnail)
                    resizeFile = ImageUtil.subSample4x(tempFile!!, 1024)
                    photoObj?.file = resizeFile
                    photoObj?.uri = tempUri.toString()
                    form_photo_name.setText(resizeFile.name)
                    form_photo_name.setTextColor(resources.getColor(R.color.colorPrimary))
                    form_photo.setText(getString(R.string.record_photo_ok))
                    tempUri = null
                    tempFile = null
                }
                REQUEST_GALLERY -> {
                    intent?.data.let {
                        Glide.with(this).load(it).into(form_photo_thumbnail)
                        val file = ImageUtil.uriToFile(this, it!!)
                        resizeFile = ImageUtil.subSample4x(file, 1024)
                        photoObj?.file = resizeFile
                        photoObj?.uri = it.toString()
                        form_photo_name.setText(resizeFile.name)
                        form_photo_name.setTextColor(resources.getColor(R.color.colorPrimary))
                        form_photo.setText(getString(R.string.record_photo_ok))
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    tempFile?.let {
                        if (it.delete()) tempFile = null
                    }
                    tempUri = null
                }
            }
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
            if (isAllValid() && isSpecValid()) try {
                val entry = setEntry()
                val previewEntries = ArrayList<Entry>()
                previewEntries.add(entry)
                startActivity(
                    Intent(this@RegisterActivity, ViewActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("PipeView", "Register")
                        .putExtra("RegisterPreview", previewEntries)
                        .putExtra("PipeIndex", 0)
                        .putExtra("fHorizontal", form_horizontal.text.toString())
                        .putExtra("fVertical", form_vertical.text.toString())
                        .putExtra("SpiPhotoObject", photoObj)
                )
            } catch (e: Exception) {
                messageDialog(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.")
            }
        }

        private fun isAllValid(): Boolean {
            var allValid = true
            val validateFields = arrayOf<FormEditText>(
                form_pipe,
                form_shape,
                form_horizontal,
                form_vertical,
                form_depth,
                form_spec,
                form_material,
                form_supervise,
                form_supervise_contact
            )
            for (field in validateFields) {
                allValid = field.testValidity() && allValid
            }
            return allValid
        }

        // TODO: 관로관경 최대값 4자리로 설정 후 4자리일 경우 콤마 추가해서 보여주도록 설정
        private fun isSpecValid(): Boolean {
            var isSpecValid = true
            if (form_spec.inputType == TYPE_CLASS_NUMBER) {
                isSpecValid = form_spec.text.toString().toDouble() < 9999.9
                if (!isSpecValid) form_spec.error = "이 범위(0.0 - 9999.9)안에 해당하는 숫자만 입력가능합니다."
            }
            return isSpecValid
        }

        private fun setEntry(): Entry {
            val spiId = spi.id
            spiMemo.spi_id = spiId
            spiMemo.memo = form_memo.text.toString()
            spiPhoto.spi_id = spiId
            spiLocation.spi_id = spiId
            pipe.spi_id = spiId
            pipe.depth = form_depth.text.toString().toDouble()
            pipe.material = form_material.text.toString()
            pipe.supervise_contact = form_supervise_contact.text.toString()
            pipe.construction = form_construction.text.toString()
            pipe.construction_contact = form_construction_contact.text.toString()
            pipeType.pipe = form_pipe.text.toString()
            pipeShape.shape = form_shape.text.toString()
            pipeShape.spec = form_spec.text.toString()
            pipePlan.file_section =
                "plan_${parseSpiType(spiType.type)}_${pipePosition.position}.png"
            pipeSupervise.supervise = form_supervise.text.toString()

            val entry = Entry(
                spi,
                spiType,
                spiMemo,
                spiPhoto,
                pipe,
                pipeType,
                pipeShape,
                pipePosition,
                pipePlan,
                pipeSupervise
            )
            entry.spi_location = spiLocation
            return entry
        }
    }
}
