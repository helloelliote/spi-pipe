package kr.djspi.pipe01;

import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;

public final class Const {

    static final String URL_SPI = "http://35.187.193.145/";
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
    public static final String[] PIPE_SHAPES = {"직진형", "T분기형", "엘보형", "관말형"};
    public static final String[] PIPE_DIRECTIONS = {null, "", "out", "", "outl", null, "outr", "", "in", ""};
    public static final PipeTypeEnum[] PIPE_TYPE_ENUMS = PipeTypeEnum.values();

    static final int REQUEST_CODE_MAP = 30001;
    static final int REQUEST_CODE_PHOTO = 10001;
    static final int REQUEST_CODE_GALLERY = 10002;

    public static final int RESULT_PASS = 200;
    public static final int RESULT_FAIL = 400;

    public static final String nfcRecord = "949 표지주 상수관로 엘보형 알루미늄 관경 145 mm 위치:3 수직m:1.44 수평m:2.55 심도m:1.45 관리:대진기술정보(주) 053-424-9545";
}
