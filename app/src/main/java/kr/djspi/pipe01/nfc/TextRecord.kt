package kr.djspi.pipe01.nfc

import androidx.core.util.Preconditions

class TextRecord(languageCode: String, text: String) : ParsedRecord {

    /**
     * ISO/IANA language code
     */
    /**
     * Returns the ISO/IANA language code associated with this text element.
     */
    val languageCode: String = Preconditions.checkNotNull(languageCode)
    val text: String = Preconditions.checkNotNull(text)

    override val type: Int
        get() = ParsedRecord.TYPE_TEXT
}
