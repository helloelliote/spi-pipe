package kr.djspi.pipe01

import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum

object Const {

    internal val URL_SPI = "http://35.200.109.228/"
    internal val URL_TEST = "http://192.168.0.33/"

    val TAG_PIPE = "pipe"
    val TAG_SHAPE = "shape"
    val TAG_SUPERVISE = "supervise"
    val TAG_POSITION = "position"
    val TAG_DIRECTION = "direction"
    val TAG_DISTANCE = "distance"
    val TAG_LOCATION = "location"
    val TAG_SURVEY = "survey"
    val TAG_PREVIEW = "preview"
    val TAG_PHOTO = "photo"
    val PIPE_SHAPES = arrayOf("직진형", "T분기형", "엘보형", "관말형")
    val PIPE_DIRECTIONS = arrayOf("", "", "out", "", "outl", "", "outr", "", "in", "")
    val PIPE_TYPE_ENUMS = PipeTypeEnum.values()

    internal val REQUEST_MAP = 30001
    internal val REQUEST_CAPTURE_IMAGE = 10001
    internal val REQUEST_GALLERY = 10002

    val RESULT_PASS = 200
    val RESULT_FAIL = 400
}
