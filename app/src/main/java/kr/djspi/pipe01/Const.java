package kr.djspi.pipe01;

public final class Const {

    public static final String TAG_PIPE = "pipe";
    public static final String TAG_SHAPE = "shape";
    public static final String TAG_SUPERVISE = "supervise";
    public static final String TAG_POSITION = "position";
    public static final String TAG_DIRECTION = "direction";
    public static final String TAG_TYPE_PLATE = "표지판";
    public static final String TAG_TYPE_MARKER = "표지기";
    public static final String TAG_TYPE_COLUMN = "표지주";
    public static final String[] PIPE_SHAPES = {"직진형", "T분기형", "엘보형", "관말형"};
    static final String[] PIPE_DIRECTIONS = {null, "nw", "n", "ne", "w", null, "e", "sw", "s", "se"};

    static final int REQUEST_CODE_MAP = 30001;
    static final int REQUEST_CODE_PHOTO = 10001;
    static final int REQUEST_CODE_GALLERY = 10002;

    static final String URL_SPI = "https://ispi.kr/";
    static final String URL_TEST = "http://192.168.0.33/";
}
