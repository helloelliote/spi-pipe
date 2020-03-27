package kr.djspi.pipe01.fragment

import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.provider.Settings.ACTION_NFC_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kr.djspi.pipe01.BaseActivity.Companion.isReadyForPost
import kr.djspi.pipe01.MainActivity
import kr.djspi.pipe01.R
import kr.djspi.pipe01.util.fromHtml
import kotlin.system.exitProcess

class MessageDialog : DialogFragment(), OnClickListener {

    private var returnToMain: Boolean = false
    private var issue: Int? = 0
    private lateinit var title: TextView
    private lateinit var contents: TextView
    private lateinit var contentsSub: TextView
    private lateinit var buttonDismiss: TextView
    private lateinit var buttonOk: TextView
    private lateinit var infoIcon: ImageView
    private lateinit var border: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        issue = arguments?.getInt("issueType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        title = view.findViewById(R.id.popup_title)
        contents = view.findViewById(R.id.popup_contents)
        contentsSub = view.findViewById(R.id.popup_contents_sub)
        buttonDismiss = view.findViewById(R.id.button_dismiss)
        buttonDismiss.setOnClickListener(this)
        buttonOk = view.findViewById(R.id.button_ok)
        buttonOk.setOnClickListener(this)
        infoIcon = view.findViewById(R.id.icon_info_s)
        border = view.findViewById(R.id.view_border)
        view.findViewById<ImageView>(R.id.button_close).setOnClickListener(this)
        setDialog(issue)
        return view
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button_dismiss -> activity?.onBackPressed()
            R.id.button_ok -> {
                if (issue == 5) {
                    isReadyForPost = true
                }
                dismiss()
            }
            R.id.button_close -> {
                returnToMain = true
                dismiss()
            }
        }
    }

    private fun setDialog(issue: Int?) {
        title.text = "알림"
        contents.text = tag
        when (issue) {
            0 -> { // 일반 메시지 전달
                setVisibilityToGone()
            }
            1 -> { // (공통) 위치 기능이 꺼져 있음
                title.text = "주의"
                contentsSub.text = fromHtml(getString(R.string.popup_location_on_sub))
                buttonOk.setOnClickListener {
                    startActivity(
                        Intent(ACTION_LOCATION_SOURCE_SETTINGS).addCategory(CATEGORY_DEFAULT)
                    )
                    dismiss()
                }
            }
            2 -> { // (공통) NFC 기능이 꺼져 있음
                title.text = "주의"
                contentsSub.text = fromHtml(getString(R.string.popup_nfc_on_sub))
                buttonOk.setOnClickListener {
                    startActivity(Intent(ACTION_NFC_SETTINGS).addCategory(CATEGORY_DEFAULT))
                    dismiss()
                }
            }
            3 -> { // (MainActivity.kt) 정품 SPI 가 아닌 태그가 태깅되었음
                setVisibilityToGone()
                title.text = "주의"
                buttonOk.text = "종료"
                buttonOk.setOnClickListener {
                    dismiss()
//                    android.os.Process.killProcess(android.os.Process.myPid())
                    System.runFinalization()
                    exitProcess(0)
                }
            }
            4 -> { // (MainActivity.kt) 태그 정보 조회 실패
                setVisibilityToGone()
            }
            5 -> { // (SpiPostActivity.kt) 쓰기 작업 이후 수정이 불가함을 안내
                setVisibilityToGone()
                title.text = "주의"
                contents.text = fromHtml(getString(R.string.popup_read_only))
                buttonDismiss.visibility = View.VISIBLE
                buttonDismiss.text = "이전"
            }
            6 -> { // (SpiPostActivity.kt) 정보가 정상적으로 기록됨
                setVisibilityToGone()
                returnToMain = true
            }
            7 -> { // (SpiPostActivity.kt) 하나 이상의 관로 정보 등록 과정에서 에러 발생 안내
                contents.text = getString(R.string.popup_error_set)
                contentsSub.text = tag
            }
            8 -> { // (공통) 서버와의 통신 에러
                contents.text = getString(R.string.popup_error_comm)
                setVisibilityToGone()
            }
            9 -> { // (MainActivity.kt) 앱 시작 시 절전모드가 실행중인지 확인
                title.text = "주의"
                contentsSub.text = getString(R.string.popup_power_save_sub)
            }
            10 -> { // (BaseActivity.kt) 위치 정보를 가져오지 못하여 지도보기를 실행하지 못함
                title.text = "주의"
                contentsSub.text = getString(R.string.popup_error_location_count_exceed_sub)
            }
            11 -> { // (MainActivity.kt) SPI 제품 초기화 개편 (선로종류, 형태, 관리기관 추가) 후 이전 초기화 제품에 대한 안내
                contents.text = fromHtml(tag!!)
                setVisibilityToGone()
            }
            12 -> { // (SpiPostActivity.kt) 정보를 등록하려는 태그와 실제 태깅된 태그가 다를 때 등록 거부
                contentsSub.text = getString(R.string.popup_error_serial_mismatch_sub)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (returnToMain) {
            if (isAdded) {
                startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(FLAG_ACTIVITY_NEW_TASK)
                )
            } else return
        } else {
            return
        }
        isReadyForPost = false
        super.onDismiss(dialog)
    }

    private fun setVisibilityToGone() {
        contentsSub.visibility = View.GONE
        infoIcon.visibility = View.GONE
        border.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun getInstance(issue: Int, cancelable: Boolean): MessageDialog {
            return MessageDialog().apply {
                arguments = Bundle().apply {
                    putInt("issueType", issue)
                }
                isCancelable = cancelable
            }
        }
    }
}
