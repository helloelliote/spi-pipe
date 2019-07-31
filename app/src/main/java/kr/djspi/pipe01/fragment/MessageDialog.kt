package kr.djspi.pipe01.fragment

import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.provider.Settings.ACTION_NFC_SETTINGS
import android.text.Html.fromHtml
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_list.popup_title
import kotlinx.android.synthetic.main.fragment_message.*
import kr.djspi.pipe01.MainActivity
import kr.djspi.pipe01.R

@Suppress("DEPRECATION")
class MessageDialog : DialogFragment(), OnClickListener {

    private var returnToMain: Boolean = false
    private var issue: Int? = 0

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
        button_dismiss.setOnClickListener(this)
        button_close.setOnClickListener(this)
        button_ok.setOnClickListener(this)
        setDialog(issue)
        return view
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button_dismiss -> activity?.onBackPressed()
            R.id.button_ok -> dismiss()
            R.id.button_close -> {
                returnToMain = true
                dismiss()
            }
        }
    }

    private fun setDialog(issue: Int?) {
        // TODO: 테스트 후 삭제
        Log.w("MessageDialog", this@MessageDialog.toString())
        popup_title.text = "알림"
        popup_contents.text = tag
        when (issue) {
            0 -> { // 일반 메시지 전달
                setVisibilityToGone()
            }
            1 -> { // (공통) 위치 기능이 꺼져 있음
                popup_title.text = "주의"
                popup_contents_sub.text = fromHtml(getString(R.string.popup_location_on_sub))
                button_ok.setOnClickListener {
                    startActivity(
                        Intent(ACTION_LOCATION_SOURCE_SETTINGS).addCategory(
                            CATEGORY_DEFAULT
                        )
                    )
                    dismiss()
                }
            }
            2 -> { // (공통) NFC 기능이 꺼져 있음
                popup_title.text = "주의"
                popup_contents_sub.text = fromHtml(getString(R.string.popup_nfc_on_sub))
                button_ok.setOnClickListener {
                    startActivity(Intent(ACTION_NFC_SETTINGS).addCategory(CATEGORY_DEFAULT))
                    dismiss()
                }
            }
            3 -> { // (MainActivity.class) 정품 SPI 가 아닌 태그가 태깅되었음
                setVisibilityToGone()
                popup_title.text = "주의"
                button_ok.text = "종료"
                button_ok.setOnClickListener {
                    dismiss()
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }
            4 -> { // (MainActivity.class) 태그 정보 조회 실패
                setVisibilityToGone()
            }
            5 -> { // (SpiPostActivity.class) 쓰기 작업 이후 수정이 불가함을 안내
                setVisibilityToGone()
                popup_title.text = "주의"
                popup_contents.text = fromHtml(getString(R.string.popup_read_only))
                button_dismiss.visibility = View.VISIBLE
                button_dismiss.text = "이전"
            }
            6 -> { // (SpiPostActivity.class) 정보가 정상적으로 기록됨
                setVisibilityToGone()
                returnToMain = true
            }
            7 -> { // (SpiPostActivity.class) 하나 이상의 관로 정보 등록 과정에서 에러 발생 안내
                popup_contents.text = getString(R.string.popup_error_set)
                popup_contents_sub.text = tag
            }
            8 -> { // (공통) 서버와의 통신 에러
                popup_contents.text = getString(R.string.popup_error_comm)
                setVisibilityToGone()
            }
            9 -> { // (MainActivity.class) 앱 시작 시 절전모드가 실행중인지 확인
                popup_title.text = "주의"
                popup_contents_sub.text = getString(R.string.popup_power_save_sub)
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
        } else return
        super.onDismiss(dialog)
    }

    private fun setVisibilityToGone() {
        popup_contents_sub.visibility = View.GONE
        icon_info_s.visibility = View.GONE
        view_border.visibility = View.GONE
    }

    companion object {

        @JvmStatic
        fun newInstance(issue: Int, cancelable: Boolean): MessageDialog {
            return MessageDialog().apply {
                arguments = Bundle().apply {
                    putInt("issueType", issue)
                }
                isCancelable = cancelable
            }
        }
    }
}
