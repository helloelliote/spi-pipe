package kr.djspi.pipe01.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import java.util.*

internal object NdefMessageParser {

    fun parse(message: NdefMessage): List<ParsedRecord> {
        return getRecords(message.records)
    }

    private fun getRecords(records: Array<NdefRecord>): List<ParsedRecord> {
        val elements = ArrayList<ParsedRecord>()
        for (record in records) {
            if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record))
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

}// Utility class
