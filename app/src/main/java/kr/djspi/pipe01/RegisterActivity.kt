package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_register.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.Const.PIPE_DIRECTIONS
import kr.djspi.pipe01.Const.REQUEST_CAPTURE_IMAGE
import kr.djspi.pipe01.Const.REQUEST_FILE_VIEWER
import kr.djspi.pipe01.Const.REQUEST_GALLERY
import kr.djspi.pipe01.Const.TAG_DIRECTION
import kr.djspi.pipe01.Const.TAG_DISTANCE
import kr.djspi.pipe01.Const.TAG_PHOTO
import kr.djspi.pipe01.Const.TAG_POSITION
import kr.djspi.pipe01.dto.*
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType
import kr.djspi.pipe01.fragment.ImageDialog
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.fragment.PhotoDialog
import kr.djspi.pipe01.fragment.PositionDialog
import kr.djspi.pipe01.util.*
import kr.djspi.pipe01.util.ImageUtil.preserveExif
import kr.djspi.pipe01.util.ImageUtil.resizeImageToRes
import kr.djspi.pipe01.util.ImageUtil.saveImageToGallery
import kr.djspi.pipe01.util.ImageUtil.uriToFilePath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.*

class RegisterActivity : BaseActivity(), OnSelectListener, View.OnClickListener, Serializable {

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
    private lateinit var lPhotoDesc: LinearLayout
    private lateinit var imageThumb: ImageView
    private lateinit var fPhoto: FormEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(R.layout.activity_register)
        restoreInstanceState()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar.title = "SPI 지중선로 ${spiType.type}"

        setOnClickListeners()
        form_horizontal.isFocusable = false
        form_vertical.isFocusable = false
        form_depth.filters = arrayOf(DecimalFilter(4, 2))
        form_supervise_contact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        form_material.setOnEditorActionListener { v, actionId, _ ->
            var isHandled = false
            if (actionId == IME_ACTION_NEXT && form_supervise_contact.text.toString() == "") {
                imm.toggleSoftInputFromWindow(v.windowToken, 0, 0)
                form_supervise_contact.requestFocus()
                isHandled = true
            }
            return@setOnEditorActionListener isHandled
        }
        form_construction_contact.addTextChangedListener(
            object : PhoneNumberFormattingTextWatcher() {})
        // 초기화 항목 지정
        runOnUiThread {
            form_pipe.setText(pipeType.pipe)
            form_shape.setText(pipeShape.shape)
            val fSpec = findViewById<FormEditText>(R.id.form_spec)
            if (pipeType.unit == "mm") {
                fSpec.inputType = TYPE_CLASS_NUMBER
            } else {
                fSpec.inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS
                fSpec.error = null
            }
            header.text = pipeType.header
            unit.text = pipeType.unit
            form_supervise.setText(pipeSupervise.supervise)
        }
    }

    private fun setOnClickListeners() {
        arrayOf(
            lay_distance,
            form_horizontal,
            form_vertical,
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
                form_material.setText(pref["material", ""])
                form_supervise_contact.setText(pref["supervise_contact", ""])
                form_construction.setText(pref["construction", ""])
                form_construction_contact.setText(pref["construction_contact", ""])
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lay_distance, R.id.form_horizontal, R.id.form_vertical -> {
                form_horizontal.text = null
                form_vertical.text = null
                showPositionDialog()
            }
            R.id.lay_photo, R.id.form_photo -> {
                lay_photo_desc.visibility = View.VISIBLE
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
                    form_photo_thumbnail.setImageDrawable(null)
                    form_photo_name.apply {
                        isFocusable = false
                        setText(getString(R.string.record_input_photo_delete))
                        setTextColor(resources.getColor(R.color.colorAccent, null))
                    }
                    form_photo.text = null
                    uri = null
                    file?.delete()
                    file = null
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        if (index == -1) return
        when (tag) {
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
                form_vertical.setText("${form_vertical.tag} ${text[1]}")
                form_depth.requestFocus()
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
                    3 -> {
                        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                        getIntent.type = "image/*"
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickIntent.type = "image/*"
                        Intent.createChooser(getIntent, getString(R.string.popup_file_viewer_title))
                            .also { intent ->
                                intent.resolveActivity(packageManager)?.also {
                                    intent.putExtra(
                                        Intent.EXTRA_INITIAL_INTENTS,
                                        arrayOf(pickIntent)
                                    )
                                    val photoFile: File? = try {
                                        createImageFile()
                                    } catch (e: IOException) {
                                        toast(getString(R.string.record_camera_error))
                                        null
                                    }
                                    photoFile?.let {
                                        startActivityForResult(intent, REQUEST_FILE_VIEWER)
                                    }
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
        PositionDialog().apply { arguments = bundle }.show(supportFragmentManager, TAG_POSITION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val file = File(currentPhotoPath)
                    val resizeFile = file.resizeImageToRes(1024).preserveExif(file)
                    Glide.with(this).load(resizeFile).into(imageThumb)
                    form_photo_name.setText(resizeFile.name)
                    form_photo_name.setTextColor(resources.getColor(R.color.colorPrimary, null))
                    fPhoto.setText(getString(R.string.record_photo_ok))
                    Thread(Runnable {
                        photoObj = SpiPhotoObject()
                        photoObj!!.file = resizeFile
                        photoObj!!.setUri(Uri.fromFile(resizeFile))
                        saveImageToGallery(file, "SPI").also {
                            if (file.exists()) file.delete()
                        }
                    }).start()
                }
                REQUEST_GALLERY -> {
                    intent?.data.run {
                        val file = File(uriToFilePath(this))
                        val resizeFile = file.resizeImageToRes(1024)
                        Glide.with(applicationContext).load(resizeFile).into(imageThumb)
                        form_photo_name.setText(resizeFile.name)
                        form_photo_name.setTextColor(resources.getColor(R.color.colorPrimary, null))
                        fPhoto.setText(getString(R.string.record_photo_ok))
                        Thread(Runnable {
                            photoObj = SpiPhotoObject()
                            photoObj!!.file = resizeFile
                            photoObj!!.setUri(this)
                        }).start()
                    }
                }
                REQUEST_FILE_VIEWER -> {
                    intent?.data.run {
                        val inputStream = contentResolver.openInputStream(this!!)
                        val file = File(currentPhotoPath)
                        // Over minSdkVersion = 26
//                        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        // OR...
                        inputStream.use { input ->
                            val outputStream = FileOutputStream(file)
                            outputStream.use { output ->
                                val buffer = ByteArray(4 * 1024) // buffer size
                                while (true) {
                                    val byteCount = input!!.read(buffer)
                                    if (byteCount < 0) break
                                    output.write(buffer, 0, byteCount)
                                }
                                output.flush()
                            }
                        }
                        val resizeFile = file.resizeImageToRes(1024)
                        Glide.with(applicationContext).load(resizeFile).into(imageThumb)
                        form_photo_name.setText(resizeFile.name)
                        form_photo_name.setTextColor(resources.getColor(R.color.colorPrimary, null))
                        fPhoto.setText(getString(R.string.record_photo_ok))
                        Thread(Runnable {
                            photoObj = SpiPhotoObject()
                            photoObj!!.file = resizeFile
                            photoObj!!.setUri(Uri.fromFile(resizeFile))
                        }).start()
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
                        putExtra("fHorizontal", form_horizontal.text.toString())
                        putExtra("fVertical", form_vertical.text.toString())
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

        private fun isSpecValid(): Boolean {
            var isSpecValid = true
            if (form_spec.inputType == TYPE_CLASS_NUMBER) {
                isSpecValid = form_spec.text.toString().toDouble() < 9999.9
                if (!isSpecValid) form_spec.error = "범위(0.0 - 9999.9)내의 숫자만 입력가능합니다."
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
            pipe.type_id = pipeType.id
            pipe.depth = form_depth.text.toString().toDouble()
            pipe.material = form_material.text.toString()
            pipe.supervise_id = pipeSupervise.id
            pipe.supervise_contact = form_supervise_contact.text.toString()
            pipe.construction = form_construction.text.toString()
            pipe.construction_contact = form_construction_contact.text.toString()
            pipeShape.spec = form_spec.text.toString()
            pipePlan.file_section =
                "plan_${parseSpiType(spiType.type)}_${pipePosition.position}.png"
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
