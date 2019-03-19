package kr.djspi.pipe01;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

public final class Const {

    public static final String API_SPI_GET = "spi-get";
    public static final String API_PIPE_GET = "pipe-get";
    public static final String API_SUPERVISE = "supervise-get";
    static final String URL_SPI = "https://ispi.kr/";
    static final String URL_TEST = "http://192.168.0.33/";

    public static final String TAG_PIPE = "pipe";
    public static final String TAG_SHAPE = "shape";
    public static final String TAG_SUPERVISE = "supervise";
    public static final String TAG_POSITION = "position";
    public static final String TAG_DIRECTION = "direction";
    public static final String TAG_TYPE_PLATE = "표지판";
    public static final String TAG_TYPE_MARKER = "표지기";
    public static final String TAG_TYPE_COLUMN = "표지주";
    public static final String[] PIPE_SHAPES = {"직진형", "T분기형", "엘보형", "관말형"};
    public static final String[] PIPE_DIRECTIONS = {null, "", "out", "", "outl", null, "outr", "", "in", ""};

    static final int REQUEST_CODE_MAP = 30001;
    static final int REQUEST_CODE_PHOTO = 10001;
    static final int REQUEST_CODE_GALLERY = 10002;

    @SuppressWarnings("ALL")
    public enum NfcRecordEnum {
        ID("spi_id", "ID:"),
        PIPE("pipe", ""),
        SHAPE("shape", ""),
        MATERIAL("material", ""),
        HEADER("header", ""),
        SPEC("spec", ""),
        UNIT("unit", ""),
        VERTICAL("vertical", "수직m:"),
        HORIZONTAL("horizontal", "수평m:"),
        DEPTH("depth", "심도m:"),
        SUPERVISE("supervise", "관리:"),
        CONTACT("supervise_contact", "");

        private String memberName;
        private String memberLabel;

        NfcRecordEnum(String memberName, String memberLabel) {
            this.memberName = memberName;
            this.memberLabel = memberLabel;
        }

        // TODO: 2019-03-19 통합형: 순차적 기록 루틴 개발
        public static String[] parseToStringArray(@NotNull JsonObject jsonObject, int index) {
            final NfcRecordEnum[] values = NfcRecordEnum.values();
            final int length = NfcRecordEnum.values().length;
            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
            JsonObject object = jsonArray.get(index).getAsJsonObject();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(values[i].memberLabel).append(object.get(values[i].memberName).getAsString()).append(" ");
            }
            String[] strings = new String[1];
            strings[0] = builder.toString();
            return strings;
        }
    }
}
