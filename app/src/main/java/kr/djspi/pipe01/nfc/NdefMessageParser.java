package kr.djspi.pipe01.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class NdefMessageParser {

    // Utility class
    private NdefMessageParser() {
    }

    static List<ParsedRecord> parse(@NotNull NdefMessage message) {
        return getRecords(message.getRecords());
    }

    private static List<ParsedRecord> getRecords(@NotNull NdefRecord[] records) {
        List<ParsedRecord> elements = new ArrayList<>();
        for (NdefRecord record : records) {
            if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record));
            }
        }

        return elements;
    }

//    사용예제:
//    Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//        if (rawMsgs == null) {
//        return null;
//    }
//    List<ParsedRecord> records = NdefMessageParser.parse((NdefMessage) rawMsgs[0]);

}
