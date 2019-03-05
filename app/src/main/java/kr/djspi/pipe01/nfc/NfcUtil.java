package kr.djspi.pipe01.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.ndef.NdefMessageWrapper;
import com.nxp.nfclib.ndef.NdefRecordWrapper;
import com.nxp.nfclib.ntag.INTag213215216;
import com.nxp.nfclib.ntag.NTagFactory;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.djspi.pipe01.R;

import static android.nfc.NdefRecord.RTD_TEXT;
import static android.nfc.NdefRecord.TNF_WELL_KNOWN;
import static com.nxp.nfclib.CardType.NTag216;
import static kr.djspi.pipe01.BuildConfig.APPLICATION_ID;
import static kr.djspi.pipe01.BuildConfig.NFC_LICENSE_KEY;
import static kr.djspi.pipe01.BuildConfig.a;
import static kr.djspi.pipe01.BuildConfig.k;
import static kr.djspi.pipe01.BuildConfig.setReadOnly;

public final class NfcUtil {

    private static final String TAG = NfcUtil.class.getSimpleName();
    private static NfcAdapter nfcAdapter;
    private static PendingIntent pendingIntent;
    private static String[][] techLists;
    private static IntentFilter[] intentFilters;
    public NxpNfcLib nxpNfcLib;
    public static INTag213215216 objNtag;

    private NfcUtil() {
    }

    private static class LazyHolder {
        static final NfcUtil INSTANCE = new NfcUtil();
    }

    public static NfcUtil getInstance(NfcAdapter nfcAdapter) {
        NfcUtil.nfcAdapter = nfcAdapter;
        return LazyHolder.INSTANCE;
    }

    public static void setDispatch(Context context, Class<?> useActivityClass) {
        pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, useActivityClass).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        try {
            intentFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "text/plain")};
            intentFilters[0].addDataType("application/kr.djspi.pipe01");
        } catch (MalformedMimeTypeException ignored) {
        }
        techLists = new String[][]{{NfcA.class.getName()}, {Ndef.class.getName()}, {MifareUltralight.class.getName()}};
    }

    /**
     * TapLinx NTAG 라이브러리를 불러옴
     */
    public NfcUtil initializeLibrary(Activity activity) {
        nxpNfcLib = NxpNfcLib.getInstance();
        try {
            nxpNfcLib.registerActivity(activity, NFC_LICENSE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public static Tag intentToTag(Intent intent) {
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHexSerial(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     * NTAG 의 모델명을 찾은 뒤 적절한 태그 객체를 리턴
     * (NTAG 216 이 아님) 에러 메시지 출력, null 리턴
     *
     * @param intent 발생한 NTAG 인텐트
     * @return objNtag 인식된 NTAG 216 태그 객체
     */
    @Nullable
    public INTag213215216 getTagType(final Intent intent) {
        try {
            if (nxpNfcLib.getCardType(intent) == NTag216) {
                objNtag = NTagFactory.getInstance().getNTAG216(nxpNfcLib.getCustomModules());
            } else {
                Log.w(TAG, "TAG is NOT NTAG 216 Type");
                return null;
            }
        } catch (NxpNfcLibException e) {
            e.getMessage();
        }
        return objNtag;
    }

    /**
     * 쓰기 대상 태그에 쓰기 작업 수행
     * (setReadOnly) build.gradle 에서 설정: release 버전(true)과 내부용 alpha 버전(false) 구분해 빌드
     *
     * @param intent 전달된 태그 인텐트
     * @return isSuccess 쓰기 작업 성공 여부
     * @see NdefRecordWrapper[] createRecord 사용자 입력값으로 NDEF 레코드를 생성해 리턴
     */
    public boolean writeTag(final Intent intent, String[] recordArray, Activity activity, Context context) {
        Log.w(TAG, "writeTag() Called");
        boolean isSuccess = false;
        try {
            objNtag = getTagType(intent);
            objNtag.getReader().connect();
            objNtag.getReader().setTimeout(2000);
            objNtag.authenticatePwd(k, a); // 비밀번호 인증
//            recordArray[INPUT_ARRAY_LENGTH] = Utilities.dumpBytes(objNtag.getUID()); // NFC 칩 시리얼번호
            if (objNtag.isPwdAuthenticated()) {
                // NDEF 메시지 생성 & 쓰기 작업 실행
                objNtag.writeNDEF(new NdefMessageWrapper(createRecord(Locale.KOREAN, true, recordArray)));
                Log.w(TAG, "NTag Written");
                if (setReadOnly) objNtag.makeCardReadOnly(); // 쓰기 후 Read-Only 로 설정
                isSuccess = true;
            }
        } catch (NullPointerException | NxpNfcLibException | IllegalArgumentException e) {
            Log.e(TAG, "Exception Thrown: writeTag()");
            Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show();
            e.getMessage();
        }
        objNtag.getReader().close();
        return isSuccess;
    }

    /**
     * (TapLinx 라이브러리) NDEF 레코드 생성함수
     * 0번 레코드(고정): SPI 도시가스 방호철판앱 패키지명("com.kr.djspi.gas01") 지정. SPI 정품 태그를 태깅할 시
     * 앱이 설치되어 있으면 앱 실행, 앱이 설치되어 있지 않으면 플레이스토어 설치페이지로 연결됨
     * 1번 레코드~: SPI 정보
     * 마지막 레코드(고정): 태깅된 NFC 칩의 UID(시리얼번호) 지정
     *
     * @param locale       텍스트 데이터 로컬 설정: (ko)
     * @param encodeInUtf8 UTF-8 인코딩으로 기록
     * @return ndefRecordWrappers 태그에 기록되는 레코드 Array
     */
    private static NdefRecordWrapper[] createRecord(Locale locale, boolean encodeInUtf8, String[] record) {
        Charset utfEncoding;
        if (encodeInUtf8) {
            utfEncoding = Charset.forName("UTF-8");
        } else {
            utfEncoding = Charset.forName("UTF-16");
        }

        final int recordLength = record.length;
        byte[][] textBytes = new byte[recordLength][];
        byte[][] data = new byte[recordLength][];
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("UTF-8"));
        int langBytesLength = langBytes.length;
        char status = (char) (0 + langBytesLength);

        NdefRecordWrapper[] ndefRecordWrappers = new NdefRecordWrapper[recordLength];

        for (int i = 1; i < recordLength; i++) {
            textBytes[i] = record[i].getBytes(utfEncoding);
            data[i] = new byte[1 + langBytesLength + textBytes[i].length]; // 0번 레코드를 위해 1자리 추가
            data[i][0] = (byte) status;
            System.arraycopy(langBytes, 0, data[i], 1, langBytesLength);
            System.arraycopy(textBytes[i], 0, data[i], 1 + langBytesLength, textBytes[i].length);
            ndefRecordWrappers[i] = new NdefRecordWrapper(TNF_WELL_KNOWN, RTD_TEXT, BigInteger.valueOf(i).toByteArray(), data[i]);
        }
        // 1번 레코드부터 생성하는 for 문이 끝난 뒤 0번 레코드(패키지명)를 생성한다.
        ndefRecordWrappers[0] = new NdefRecordWrapper(NdefRecord.createApplicationRecord(APPLICATION_ID));
        return ndefRecordWrappers;
    }

    /**
     * SPI 태그에 기록된 정보를 불러옴
     * (정보가 기록되지 않음) null 을 리턴
     *
     * @return 기록된 정보를 담은 ArrayList<String>
     * @throws NullPointerException (태그 없음) 정보를 가져오는 과정에서 null 참조
     */
    public static ArrayList<String> getRecord(@NotNull Intent intent) throws NullPointerException {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        List<ParsedRecord> records = NdefMessageParser.parse((NdefMessage) rawMsgs[0]);
        ArrayList<String> recordList = new ArrayList<>();
        recordList.add(0, null);
        final int listLength = records.size() + 1;
        for (int j = 1; j < listLength; j++) {
            recordList.add(j, ((TextRecord) records.get(j - 1)).getText());
        }
        return recordList;
    }

    /**
     * 전달 받은 Tag 에서 데이터를 가져와 리턴
     *
     * @param tag
     * @return
     */
    // FIXME: 2018-12-22 StringBuilder 사용하기
    public String readTag(Tag tag) {
        String ret = "";
        try {
            Ndef ndefTag = Ndef.get(tag);
            NdefMessage ndefMessage = ndefTag.getCachedNdefMessage();
            NdefRecord[] ndefRecords = ndefMessage.getRecords();
            if (ndefRecords.length > 0) {
                for (NdefRecord item : ndefRecords) {
                    try {
                        if (new String(item.getType()).equals(new String(NdefRecord.RTD_TEXT))) {
                            ret += new String(item.getPayload(), Charset.forName("UTF-8")).substring("\nko".length());
                        } else if (new String(item.getType()).equals(new String(NdefRecord.RTD_URI))) {
                            ret += new String(item.getPayload(), Charset.forName("UTF-8"));
                        } else /*NdefRecord.RTD_ANDROID_APP*/ if (new String(item.getType()).equals("android.com:pkg")) {
                            ret += new String(item.getPayload());
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * NFC 기능 동작 확인
     * (NFC 꺼짐) 사용자가 NFC 기능을 켤 수 있게 팝업 생성
     */
    public static boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public void onResume(Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists);
        }
    }

    public void onPause(Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }
}
