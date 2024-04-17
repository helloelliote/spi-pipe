package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.andreabaccega.widget.FormEditText
import com.bumptech.glide.Glide
//import kotlinx.android.synthetic.main.activity_base.*
//import kotlinx.android.synthetic.main.activity_register.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS_ELB135
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS_VALVE
import kr.djspi.pipe01.Const.REQUEST_CAPTURE_IMAGE
import kr.djspi.pipe01.Const.REQUEST_GALLERY
import kr.djspi.pipe01.Const.TAG_DIRECTION
import kr.djspi.pipe01.Const.TAG_DIRECTION_ELB135
import kr.djspi.pipe01.Const.TAG_DIRECTION_VALVE
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.Const.TAG_POSITION
import kr.djspi.pipe01.databinding.ActivityRegisterBinding
import kr.djspi.pipe01.dto.*
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType
import kr.djspi.pipe01.fragment.*
import kr.djspi.pipe01.util.*
import kr.djspi.pipe01.util.ImageUtil.preserveExif
import kr.djspi.pipe01.util.ImageUtil.resizeImageToRes
import kr.djspi.pipe01.util.ImageUtil.saveImageToGallery
import kr.djspi.pipe01.util.ImageUtil.uriToFilePath
import java.io.File
import java.io.IOException
import java.io.Serializable

class RegisterActivity : BaseActivity(), OnSelectListener, View.OnClickListener, Serializable {

    private lateinit var registerBinding: ActivityRegisterBinding
    private lateinit var spi: Spi
    private lateinit var spiType: SpiType
    private lateinit var pipeType: PipeType
    private lateinit var pipeShape: PipeShape
    private lateinit var pipeSupervise: PipeSupervise
    private lateinit var spiMemo: SpiMemo
    private lateinit var spiPhoto: SpiPhoto
    private lateinit var spiLocation: SpiLocation
    private lateinit var pipeLocation: PipeLocation
    private lateinit var imm: InputMethodManager
    private lateinit var currentPhotoPath: String
    private var photoObj: SpiPhotoObject? = null
    private val pipe: Pipe = Pipe()
    private val pipePosition = PipePosition()
    private val pipePlan = PipePlan()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)

        if (intent != null) {
            val extra = intent.getSerializableExtra("RegisterActivity")
            if (extra is HashMap<*, *>) {
                spi = extra["Spi"] as Spi
                spiType = extra["SpiType"] as SpiType
                pipeType = extra["PipeType"] as PipeType
                pipeShape = extra["PipeShape"] as PipeShape
                pipeSupervise = extra["PipeSupervise"] as PipeSupervise
                spiLocation = extra["SpiLocation"] as SpiLocation
                pipeLocation = extra["PipeLocation"] as PipeLocation
                spiMemo = extra["SpiMemo"] as SpiMemo
                spiPhoto = extra["SpiPhoto"] as SpiPhoto
            }
        }
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        restoreInstanceState()

        binding.toolbar.title =
            if (pipeShape.shape == "제수변") "SPI 제수변 ${spiType.type}" else "SPI 지중선로 ${spiType.type}"

        setOnClickListeners()

        registerBinding.formShape.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                pipeShape.shape = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        registerBinding.formHorizontal.isFocusable = false
        registerBinding.formVertical.isFocusable = false
        registerBinding.formDepth.filters = arrayOf(DecimalFilter(4, 2))
        registerBinding.formSuperviseContact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        registerBinding.formMaterial.setOnEditorActionListener { v, actionId, _ ->
            var isHandled = false
            if (actionId == IME_ACTION_NEXT && registerBinding.formSuperviseContact.text.toString() == "") {
                imm.toggleSoftInputFromWindow(v.windowToken, 0, 0)
                registerBinding.formSuperviseContact.requestFocus()
                isHandled = true
            }
            return@setOnEditorActionListener isHandled
        }
        registerBinding.formConstructionContact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        // 초기화 항목 지정
        runOnUiThread {
            registerBinding.formPipe.setText(pipeType.pipe)
            if (pipeShape.shape == PipeShape.PipeShapeEnum.선택형.type) registerBinding.formShape.text =
                null
            else registerBinding.formShape.setText(pipeShape.shape)
            val fSpec = findViewById<FormEditText>(R.id.form_spec)
            when (pipeType.pipe) {
                "도시가스", "가스관로", "상수관로", "난방관로", "유류관로", "기타관로" -> {
                    fSpec.inputType = TYPE_CLASS_NUMBER
                }

                else -> {
                    fSpec.inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    fSpec.error = null
                }
            }
            registerBinding.header.text = pipeType.header
            registerBinding.unit.text = pipeType.unit
            registerBinding.formSupervise.setText(pipeSupervise.supervise)
        }
    }

    private fun setOnClickListeners() {
        arrayOf(
            registerBinding.layDistance,
            registerBinding.formHorizontal,
            registerBinding.formVertical,
            registerBinding.layPhoto,
            registerBinding.layPhotoDesc
        ).forEach {
            it.setOnClickListener(this)
        }

        if (pipeShape.shape == PipeShape.PipeShapeEnum.선택형.type) {
            arrayOf(
                registerBinding.layShape,
                registerBinding.formShape
            ).forEach {
                it.setOnClickListener(this)
            }
        }

        registerBinding.formPhoto.setOnClickListener(this)
        registerBinding.layPhotoDesc.setOnClickListener(this)
        registerBinding.layPhotoDesc.visibility = View.GONE
        registerBinding.formPhotoThumbnail.setOnClickListener(this)
        registerBinding.formPhotoName.isFocusable = false
        registerBinding.btnDelete.setOnClickListener(this)
        registerBinding.buttonNext.setOnClickListener(OnNextButtonClick())
    }

    private fun restoreInstanceState() {
        val pref = AppPreference.defaultPrefs(this)
        if (pref["switch_preset", false]!!) {
            runOnUiThread {
                registerBinding.apply {
                    formMaterial.setText(pref["material", ""])
                    formSuperviseContact.setText(pref["supervise_contact", ""])
                    formConstruction.setText(pref["construction", ""])
                    formConstructionContact.setText(pref["construction_contact", ""])
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lay_shape, R.id.form_shape -> {
                pipeShape.shape = null
                registerBinding.formShape.text = null
                ListDialog().show(supportFragmentManager, Const.TAG_SHAPE)
            }

            R.id.lay_distance, R.id.form_horizontal, R.id.form_vertical -> {
                registerBinding.formHorizontal.text = null
                registerBinding.formVertical.text = null
                if (registerBinding.formShape.text.toString() == "") {
                    ListDialog().show(supportFragmentManager, Const.TAG_SHAPE)
                } else {
                    showPositionDialog()
                }
            }

            R.id.lay_photo, R.id.form_photo -> {
                registerBinding.layPhotoDesc.visibility = View.VISIBLE
                PhotoDialog().show(supportFragmentManager, TAG_PHOTO)
            }

            R.id.form_photo_thumbnail -> {
                photoObj?.run {
                    val bundle = Bundle()
                    bundle.putSerializable("PhotoObj", this)
                    ImageDialog().apply {
                        arguments = bundle
                    }.show(supportFragmentManager, TAG_PHOTO)
                }
            }

            R.id.btn_delete -> {
                photoObj?.run {
                    registerBinding.formPhotoThumbnail.setImageDrawable(null)
                    registerBinding.formPhotoName.apply {
                        isFocusable = false
                        setText(getString(R.string.record_input_photo_delete))
                        setTextColor(resources.getColor(R.color.colorAccent, null))
                    }
                    registerBinding.formPhoto.text = null
                    uri = null
                    file?.delete()
                    file = null
                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "QueryPermissionsNeeded")
    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        if (index == -1) return
        when (tag) {
            Const.TAG_SHAPE -> {
                registerBinding.formShape.setText(PipeShape.PipeShapeEnum.values()[index].type)
                registerBinding.formHorizontal.text = null
                registerBinding.formVertical.text = null
                showPositionDialog()
            }

            TAG_POSITION -> {
                pipePosition.position = index
                when (index) {
                    1 -> {
                        registerBinding.formHorizontal.tag = "좌측"
                        registerBinding.formVertical.tag = "전면"
                    }

                    2 -> {
                        registerBinding.formHorizontal.tag = ""
                        registerBinding.formVertical.tag = "전면"
                    }

                    3 -> {
                        registerBinding.formHorizontal.tag = "우측"
                        registerBinding.formVertical.tag = "전면"
                    }

                    4 -> {
                        registerBinding.formHorizontal.tag = "좌측"
                        registerBinding.formVertical.tag = ""
                    }

                    5 -> {
                        registerBinding.formHorizontal.tag = "직상"
                        registerBinding.formVertical.tag = "직상"
                        registerBinding.formHorizontal.setText("0.0")
                        registerBinding.formVertical.setText("0.0")
                        pipePosition.horizontal = 0.0
                        pipePosition.vertical = 0.0
                    }

                    6 -> {
                        registerBinding.formHorizontal.tag = "우측"
                        registerBinding.formVertical.tag = ""
                    }

                    7 -> {
                        registerBinding.formHorizontal.tag = "좌측"
                        registerBinding.formVertical.tag = "후면"
                    }

                    8 -> {
                        registerBinding.formHorizontal.tag = ""
                        registerBinding.formVertical.tag = "후면"
                    }

                    9 -> {
                        registerBinding.formHorizontal.tag = "우측"
                        registerBinding.formVertical.tag = "후면"
                    }

                    else -> {
                        registerBinding.formHorizontal.tag = "수평"
                        registerBinding.formVertical.tag = "수직"
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

            TAG_DIRECTION_ELB135 -> {
                if (index == -2) {
                    showPositionDialog()
                    return
                }
                pipePosition.direction = PIPE_DIRECTIONS_ELB135[index]
                pipePlan.file_plane = "${text[0]}.png"
            }

            TAG_DIRECTION_VALVE -> {
                if (index == -2) {
                    showPositionDialog()
                    return
                }
                pipePosition.direction = PIPE_DIRECTIONS_VALVE[2]
                pipePlan.file_plane = "${text[0]}.png"
            }

            TAG_DISTANCE -> {
                if (index == -2) { // 사용자가 이전 다이얼로그에서 '취소' 선택
                    showPositionDialog()
                    return
                }
                registerBinding.formHorizontal.setText("${registerBinding.formHorizontal.tag} ${text[0]}")
                registerBinding.formVertical.setText("${registerBinding.formVertical.tag} ${text[1]}")
                registerBinding.formDepth.requestFocus()
                pipePosition.horizontal = text[0]!!.toDouble()
                pipePosition.vertical = text[1]!!.toDouble()
                imm.toggleSoftInput(SHOW_IMPLICIT, HIDE_NOT_ALWAYS)
            }

            TAG_PHOTO -> {
                when (index) {
                    1 -> {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                            intent.resolveActivity(packageManager)?.also {
                                val photoFile: File? = try {
                                    createImageFile()
                                } catch (e: IOException) {
                                    toast(getString(R.string.record_camera_error))
                                    null
                                }
                                photoFile?.also {
                                    val photoURI: Uri =
                                        FileProvider.getUriForFile(this, packageName, it)
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
                                }
                            }
                        }
                    }

                    2 -> {
                        Intent(Intent.ACTION_PICK).also { intent ->
                            intent.resolveActivity(packageManager)?.also {
                                intent.setDataAndType(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.Images.Media.CONTENT_TYPE
                                )
                                startActivityForResult(intent, REQUEST_GALLERY)
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("TEMP_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showPositionDialog() {
        val bundle = Bundle()
        bundle.putString("typeString", spiType.type)
        bundle.putString("shapeString", pipeShape.shape)
        PositionDialog().apply {
            arguments = bundle
        }
            .show(supportFragmentManager, TAG_POSITION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val file = File(currentPhotoPath)
                    val resizeFile = file
                        .resizeImageToRes(this, 1024)
                        .preserveExif(file)
                    Glide
                        .with(this)
                        .load(resizeFile)
                        .into(registerBinding.formPhotoThumbnail)
                    registerBinding.formPhotoName.setText(resizeFile.name)
                    registerBinding.formPhotoName.setTextColor(
                        resources.getColor(
                            R.color.colorPrimary,
                            null,
                        )
                    )
                    registerBinding.formPhoto.setText(getString(R.string.record_photo_ok))
                    Thread {
                        photoObj = SpiPhotoObject()
                        photoObj!!.file = resizeFile
                        photoObj!!.setUri(Uri.fromFile(resizeFile))
                        saveImageToGallery(file, "SPI").also {
                            if (file.exists()) file.delete()
                        }
                    }.start()
                }

                REQUEST_GALLERY -> {
                    intent?.data.let { uri ->
                        println("intent: $intent")
                        println("intent.data: ${intent?.data}")
                        println("?this?: $uri")
                        val file = File(uriToFilePath(uri))
                        println("file: ${file.absolutePath}")
                        println("file: ${file.parent}")
                        val resizeFile = file.resizeImageToRes(
                            this,
                            1024,
                        )
                        Glide
                            .with(applicationContext)
                            .load(resizeFile)
                            .into(registerBinding.formPhotoThumbnail)
                        registerBinding.formPhotoName.setText(resizeFile.name)
                        registerBinding.formPhotoName.setTextColor(
                            ContextCompat.getColor(
                                this@RegisterActivity,
                                R.color.colorPrimary
                            )
                        )
                        Thread {
                            photoObj = SpiPhotoObject()
                            photoObj!!.file = resizeFile
                            photoObj!!.setUri(uri)
                        }.start()
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            toast(getString(R.string.record_photo_error))
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
                Intent(this@RegisterActivity, ViewActivity::class.java)
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("PipeView", "Register")
                        putExtra("RegisterPreview", previewEntries)
                        putExtra("PipeIndex", 0)
                        putExtra("fHorizontal", registerBinding.formHorizontal.text.toString())
                        putExtra("fVertical", registerBinding.formVertical.text.toString())
                        putExtra("PhotoObj", photoObj)
                    }.also {
                        startActivity(it)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                messageDialog(0, "다음 단계로 진행할 수 없습니다.\n입력값을 다시 확인해 주세요.")
            }
        }

        private fun isAllValid(): Boolean {
            var allValid = true
            try {
                registerBinding.apply {
                    val validateFields = arrayOf<FormEditText>(
                        formPipe,
                        formShape,
                        formHorizontal,
                        formVertical,
                        formDepth,
                        formSpec,
                        formMaterial,
                        formSupervise,
                        formSuperviseContact
                    )
                    for (field in validateFields) {
                        allValid = field.testValidity() && allValid
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("INVALID: isAllValid()", "Required fields have not been completed yet!")
            }
            return allValid
        }

        private fun isSpecValid(): Boolean {
            var isSpecValid = true
            try {
                if (registerBinding.formSpec.inputType == TYPE_CLASS_NUMBER) {
                    isSpecValid = registerBinding.formSpec.text.toString().toDouble() < 9999.9
                    if (!isSpecValid) registerBinding.formSpec.error =
                        "범위(0.0 - 9999.9)내의 숫자만 입력가능합니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("INVALID: isSpecValid()", "Required fields have not been completed yet!")
            }
            return isSpecValid
        }

        private fun setEntry(): Entry {
            val spiId = spi.id
            spiMemo.spi_id = spiId
            spiMemo.memo = registerBinding.formMemo.text.toString().trim()
            spiPhoto.spi_id = spiId
            spiLocation.spi_id = spiId
            pipe.spi_id = spiId
            pipe.type_id = pipeType.id
            pipe.depth = registerBinding.formDepth.text.toString().trim().toDouble()
            pipe.material = registerBinding.formMaterial.text.toString().trim().replace(" ", "^")
            pipe.supervise_id = pipeSupervise.id
            pipe.supervise_contact = registerBinding.formSuperviseContact.text.toString().trim()
            pipe.construction = registerBinding.formConstruction.text.toString().trim()
            pipe.construction_contact =
                registerBinding.formConstructionContact.text.toString().trim()
            pipeShape.shape = registerBinding.formShape.text.toString().trim()
            pipeShape.spec = registerBinding.formSpec.text.toString().trim().replace(" ", "^")
            pipePlan.file_section =
                if (pipeShape.shape == "제수변")
                    "plan_${parseSpiType(spiType.type)}_${pipePosition.position}_valve.png"
                else "plan_${parseSpiType(spiType.type)}_${pipePosition.position}.png"
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
            entry.pipe_location = pipeLocation
            return entry
        }
    }
}
