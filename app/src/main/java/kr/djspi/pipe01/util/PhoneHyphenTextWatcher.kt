package kr.djspi.pipe01.util

import android.text.Editable
import android.text.TextWatcher

class PhoneHyphenTextWatcher: TextWatcher {

    private var isFormatting = false
    private var previousText = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting || s == null) return

        val digits = s.toString().replace(Regex("[^\\d]"), "")
        isFormatting = true

        val formatted = formatKoreanPhoneNumber(digits)
        s.replace(0, s.length, formatted)

        isFormatting = false
    }

    private fun formatKoreanPhoneNumber(digits: String): String {
        return when {
            // 휴대폰 번호 (010, 011, 016~019)
            digits.startsWith("01") && digits.length >= 10 -> {
                if (digits.length >= 11)
                    "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                else
                    "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
            }

            // 서울 번호 (02) -> 2자리 지역번호
            digits.startsWith("02") && digits.length >= 9 -> {
                if (digits.length >= 10)
                    "${digits.substring(0, 2)}-${digits.substring(2, 6)}-${digits.substring(6, 10)}"
                else
                    "${digits.substring(0, 2)}-${digits.substring(2, 5)}-${digits.substring(5)}"
            }

            // 기타 지역번호 (031, 051 등) -> 3자리 지역번호
            digits.length >= 10 -> {
                "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, 10)}"
            }

            // 기타 짧은 경우
            digits.length >= 7 -> {
                "${digits.substring(0, 3)}-${digits.substring(3)}"
            }

            else -> digits
        }
    }
}