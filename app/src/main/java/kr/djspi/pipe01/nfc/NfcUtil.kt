package kr.djspi.pipe01.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.MalformedMimeTypeException
import android.nfc.NdefMessage
import android.nfc.NdefRecord.*
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.util.Log
import com.nxp.nfclib.CardType.NTag213
import com.nxp.nfclib.CardType.NTag216
import com.nxp.nfclib.NxpNfcLib
import com.nxp.nfclib.exceptions.NxpNfcLibException
import com.nxp.nfclib.ndef.NdefMessageWrapper
import com.nxp.nfclib.ndef.NdefRecordWrapper
import com.nxp.nfclib.ntag.INTag213215216
import com.nxp.nfclib.ntag.NTagFactory
import kr.djspi.pipe01.BuildConfig.*
import java.nio.charset.Charset
import java.util.*

class NfcUtil(private val activity: Activity, useActivityClass: Class<*>) {

    private lateinit var intentFilters: Array<IntentFilter>
    private var nxpNfcLib: NxpNfcLib? = null

    /**
     * 아래의 변수들은 반드시 final 선언해야만 하며, 그렇지 않을 경우 intent 들 간의 간섭이 발생하여
     * NFC 태그를 태깅하면 이전 액티비티 인텐트를 실행하기도 한다.
     */
    private val pendingIntent: PendingIntent
    private val techLists: Array<Array<String>>
    private var objNtag: INTag213215216? = null
    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(this.activity)

    init {
        try {
            intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
//            intentFilters[0].addDataType("application/kr.djspi.pipe01")
        } catch (ignore: MalformedMimeTypeException) {
        }

        pendingIntent = PendingIntent.getActivity(
            this.activity,
            0,
            Intent(this.activity, useActivityClass).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )
        techLists = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(MifareUltralight::class.java.name)
        )

        initializeLibrary(this.activity)
    }

    /**
     * TapLinx NTAG 라이브러리를 불러옴
     */
    private fun initializeLibrary(activity: Activity) {
        try {
            nxpNfcLib = NxpNfcLib.getInstance()
            nxpNfcLib?.registerActivity(activity, NFC_APP_KEY, NFC_OFFLINE_KEY)
        } catch (e: Exception) {
        }
    }

    /**
     * NTAG 의 모델명을 찾은 뒤 적절한 태그 객체를 리턴
     * (NTAG 216 이 아님) 에러 메시지 출력, null 리턴
     *
     * @param intent 발생한 NTAG 인텐트
     * @return objNtag 인식된 NTAG 216 태그 객체
     */
    private fun getNtag(intent: Intent): INTag213215216? {
        try {
            objNtag = when {
                nxpNfcLib?.getCardType(intent) == NTag216
                -> NTagFactory.getInstance().getNTAG216(nxpNfcLib?.customModules)

                nxpNfcLib?.getCardType(intent) == NTag213
                -> NTagFactory.getInstance().getNTAG213(nxpNfcLib?.customModules)

                else -> {
                    Log.w("NfcUtil", "Tag is NOT NTAG 216 Type")
                    return null
                }
            }
        } catch (e: NxpNfcLibException) {
            e.message
        }
        return objNtag
    }

    /**
     * 쓰기 대상 태그에 쓰기 작업 수행
     * (setReadOnly) build.gradle 에서 설정: release 버전(true)과 내부용 alpha 버전(false) 구분해 빌드
     *
     * @param intent 전달된 태그 인텐트
     * @return isSuccess 쓰기 작업 성공 여부
     * @see NdefRecordWrapper[] createRecord 사용자 입력값으로 NDEF 레코드를 생성해 리턴
     */
    fun writeTag(intent: Intent, strings: Array<String?>): Boolean {
        var isSuccess = false
        try {
            objNtag = getNtag(intent)
            objNtag?.let {
                it.reader.connect()
                it.reader.timeout = 2000
                it.authenticatePwd(k, a) // 비밀번호 인증
                if (it.isPwdAuthenticated) {
                    // NDEF 메시지 생성 & 쓰기 작업 실행
                    it.writeNDEF(NdefMessageWrapper(createRecord(records = strings)))
                    Log.w("NfcUtil", "NTag Written")
                    if (setReadOnly) it.makeCardReadOnly() // 쓰기 후 Read-Only 로 설정
                    isSuccess = true
                }
            }
        } catch (e: NullPointerException) {
            Log.e("NfcUtil", "Exception Thrown: writeTag()")
            e.message
        } catch (e: NxpNfcLibException) {
            Log.e("NfcUtil", "Exception Thrown: writeTag()")
            e.message
        } catch (e: IllegalArgumentException) {
            Log.e("NfcUtil", "Exception Thrown: writeTag()")
            e.message
        }
        objNtag?.reader?.close()
        return isSuccess
    }

    /**
     * (TapLinx 라이브러리) NDEF 레코드 생성함수
     * 0번 레코드(고정): 앱 패키지명 지정. SPI 정품 태그를 태깅할 시
     * 앱이 설치되어 있으면 앱 실행, 앱이 설치되어 있지 않으면 플레이스토어 설치페이지로 연결됨
     * 1번 레코드~: SPI 정보
     * 마지막 레코드(고정): 태깅된 NFC 칩의 UID(시리얼번호) 지정
     *
     * @param locale 텍스트 데이터 로컬 설정: (ko)
     * @param utf8 UTF-8 인코딩으로 기록
     * @return wrappers 태그에 기록되는 레코드 Array
     */
    private fun createRecord(
        locale: Locale = Locale.KOREAN,
        utf8: Boolean = true,
        records: Array<String?>
    ): Array<NdefRecordWrapper?> {
        val encoding: Charset = if (utf8) Charsets.UTF_8 else Charsets.UTF_16
        val length: Int = records.size
        val texts: Array<ByteArray?> = arrayOfNulls(length)
        val data: Array<ByteArray?> = arrayOfNulls(length)
        val langBytes = locale.language.toByteArray(Charsets.UTF_8)
        val langBytesLength: Int = langBytes.size
        val status: Char = (0 + langBytesLength).toChar()
        val wrappers = arrayOfNulls<NdefRecordWrapper>(length + 1)
        for (i in 0 until length) {
            texts[i] = records[i]!!.toByteArray(encoding)
            data[i] = ByteArray(langBytesLength + texts[i]!!.size + 1) // 0번 레코드를 위해 1자리 추가
            data[i]!![0] = status.code.toByte()
            System.arraycopy(langBytes, 0, data[i]!!, 1, langBytesLength)
            System.arraycopy(texts[i] as Any, 0, data[i]!!, 1 + langBytesLength, texts[i]!!.size)
            wrappers[i + 1] = NdefRecordWrapper(
                TNF_WELL_KNOWN,
                RTD_TEXT,
                i.toString().toByteArray(),
                data[i]
            )
        }
        // 1번 레코드부터 생성하는 for 문이 끝난 뒤 0번 레코드(패키지명)를 생성한다.
        wrappers[0] = NdefRecordWrapper(createApplicationRecord(APPLICATION_ID))
        return wrappers
    }

    /**
     * 태그에 기록된 정보를 Record 단위로 불러오며, Record index 와 ArrayList 의 index 는 1:1 대응된다.
     * (정보가 기록되지 않음) null 을 리턴
     *
     * @return 기록된 정보를 담은 ArrayList<String>
     * @throws NullPointerException (태그 없음) 정보를 가져오는 과정에서 null 참조
    </String> */
    @Throws(NullPointerException::class)
    fun getRecord(intent: Intent): ArrayList<String> {
        val recordList = ArrayList<String>()
        val rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMsg?.let {
            val records = NdefMessageParser.parse(it[0] as NdefMessage)
            recordList.add(0, "")
            val listLength = records.size + 1
            for (i in 1 until listLength) {
                recordList.add(i, (records[i - 1] as TextRecord).text)
            }
        }
        return recordList
    }

    @Throws(NullPointerException::class)
    fun getSerial(intent: Intent): String {
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            ?: throw NullPointerException("Not Found")
        val bytes: ByteArray = tag.id
        val stringBuilder = StringBuilder(0)
        for (byte in bytes) {
            stringBuilder.append(String.format("%02X", byte))
        }
        val serial = stringBuilder.toString().replace("(..)".toRegex(), "$1:")
        return serial.substring(0, serial.length - 1)
    }

    /**
     * NFC 기능 동작 확인
     * (NFC 꺼짐) 사용자가 NFC 기능을 켤 수 있게 팝업 생성
     */
    fun isNfcEnabled(): Boolean {
        return nfcAdapter != null && nfcAdapter!!.isEnabled
    }

    fun onNewTagIntent(intent: Intent): Tag {
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
    }

    fun onResume() {
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists)
    }

    fun onPause() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    /**
     * Reserved for spi-init
     *
    @Throws(IOException::class, FormatException::class)
    fun writeInitTag(
    intent: Intent,
    strings: Array<String?>
    ): Boolean {
    val length = strings.size
    val ndefRecords = arrayOfNulls<NdefRecord>(length)
    ndefRecords[0] = createApplicationRecord(strings[0])
    for (i in 1 until length) {
    ndefRecords[ i ] = createTextRecord(null, strings[ i ])
    }
    val ndefMessage = NdefMessage(ndefRecords)

    // 초기화 정보를 기록
    val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
    val ndef = Ndef.get(tag)
    if (ndef != null) {
    ndef.connect()
    if (ndef.isWritable && ndef.maxSize >= ndefMessage.toByteArray().size) {
    ndef.writeNdefMessage(ndefMessage)
    ndef.close()
    return true
    }
    } else {
    val format = NdefFormatable.get(tag)
    if (format != null) {
    format.connect()
    format.format(ndefMessage)
    format.close()
    return true
    }
    }
    return false
    }

    fun readTag(tag: Tag): String {
    val ret = StringBuilder()
    try {
    val ndefTag = Ndef.get(tag)
    val ndefMessage = ndefTag.cachedNdefMessage
    val ndefRecords = ndefMessage.records
    if (ndefRecords.isNotEmpty()) {
    ndefRecords.forEach {
    when {
    String(it.type) == String(RTD_TEXT)
    -> ret.append(String(it.payload, Charsets.UTF_8).substring("\nko".length))
    String(it.type) == String(RTD_URI)
    -> ret.append(String(it.payload, Charsets.UTF_8))
    String(it.type) == "android.com:pkg"
    -> ret.append(String(it.payload))
    }
    }
    }
    } catch (ignore: Exception) {
    }
    return ret.toString()
    }

    fun setNtagPwdProtection(intent: Intent, setReadOnly: Boolean): Boolean {
    return try {
    val tag: INTag213215216? = getNtag(intent)
    tag?.programPWDPack(k, a)
    tag?.enablePasswordProtection(false, 4)
    if (setReadOnly) {
    tag?.makeCardReadOnly()
    }
    true
    } catch (e: Exception) {
    false
    }
    }

    @Throws(NullPointerException::class, NxpNfcLibException::class)
    fun clearTag(tag: INTag213215216?): Boolean {
    if (tag == null) {
    throw NullPointerException("Not Found")
    }
    tag.authenticatePwd(k, a)
    return if (tag.isPwdAuthenticated) {
    tag.enablePasswordProtection(false, 255)
    tag.clear()
    true
    } else {
    throw NxpNfcLibException("Auth Fail")
    }
    }

    @Throws(NullPointerException::class, NxpNfcLibException::class)
    fun getCardType(intent: Intent): CardType? {
    val type = nxpNfcLib?.getCardType(intent)
    if (type == CardType.UnknownCard) {
    throw NxpNfcLibException("Bad Type")
    }
    return when (type) {
    NTag213 -> NTag213
    NTag216 -> NTag216
    else -> throw IllegalArgumentException("Bad Type")
    }
    }
     *
     */
}
