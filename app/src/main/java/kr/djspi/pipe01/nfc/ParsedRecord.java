package kr.djspi.pipe01.nfc;

@SuppressWarnings("ALL")
public interface ParsedRecord {

    int TYPE_TEXT = 1;
    int TYPE_URI = 2;

    int getType();

}
