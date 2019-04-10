package kr.djspi.pipe01;

import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;

public final class Const {

    public static final String URL_SPI = "http://35.187.193.145/";
    static final String URL_TEST = "http://192.168.0.33/";

    public static final String TAG_PIPE = "pipe";
    public static final String TAG_SHAPE = "shape";
    public static final String TAG_SUPERVISE = "supervise";
    public static final String TAG_POSITION = "position";
    public static final String TAG_DIRECTION = "direction";
    public static final String TAG_DISTANCE = "distance";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_SURVEY = "survey";
    public static final String TAG_PREVIEW = "preview";
    public static final String TAG_PHOTO = "photo";
    public static final String[] PIPE_SHAPES = {"직진형", "T분기형", "엘보형", "관말형"};
    public static final String[] PIPE_DIRECTIONS = {null, "", "out", "", "outl", null, "outr", "", "in", ""};
    public static final PipeTypeEnum[] PIPE_TYPE_ENUMS = PipeTypeEnum.values();

    static final int REQUEST_MAP = 30001;
    static final int REQUEST_CAPTURE_IMAGE = 10001;
    static final int REQUEST_GALLERY = 10002;

    public static final int RESULT_PASS = 200;
    public static final int RESULT_FAIL = 400;
}
