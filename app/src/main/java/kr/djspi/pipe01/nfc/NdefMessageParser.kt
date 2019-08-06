package kr.djspi.pipe01.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import androidx.core.util.Preconditions.checkArgument
import java.util.*
import kotlin.experimental.and

object NdefMessageParser {

    fun parse(message: NdefMessage): List<ParsedRecord> {
        return getRecords(message.records)
    }

    private fun getRecords(records: Array<NdefRecord>): List<ParsedRecord> {
        val elements = ArrayList<ParsedRecord>()
        records.forEach {
            when {
                it.isText() -> elements.add(it.parse())
            }
        }
        return elements
    }

    //    사용예제:
    //    Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    //        if (rawMsgs == null) {
    //        return null;
    //    }
    //    List<ParsedRecord> records = NdefMessageParser.parse((NdefMessage) rawMsgs[0]);

    private fun NdefRecord.isText(): Boolean {
        return try {
            checkArgument(this.tnf == NdefRecord.TNF_WELL_KNOWN)
            checkArgument(Arrays.equals(this.type, NdefRecord.RTD_TEXT))
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun NdefRecord.parse(): TextRecord {
        val payload: ByteArray = this.payload
        /*
     * payload[0] contains the "Status Byte Encodings" field, per the
     * NFC Forum "Text Record Type Definition" section 3.2.1.
     *
     * bit7 is the Text Encoding Field.
     *
     * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
     * The text is encoded in UTF16
     *
     * Bit_6 is reserved for future use and must be setData to zero.
     *
     * Bits 5 to 0 are the length of the IANA language code.
     */
        val languageCodeLength = (payload[0] and 63).toInt()
        val textEncoding =
            if ((payload[0] and 128.toByte()).toInt() == 0) Charsets.UTF_8 else Charsets.UTF_16
        val languageCode = String(
            payload,
            1,
            languageCodeLength,
            Charsets.US_ASCII
        )
        val text = String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            textEncoding
        )
        return TextRecord(languageCode, text)
    }
}
