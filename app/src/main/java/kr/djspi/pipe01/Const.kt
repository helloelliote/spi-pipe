package kr.djspi.pipe01

import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum

object Const {
    const val TAG_PIPE = "pipe"
    const val TAG_SHAPE = "shape"
    const val TAG_SUPERVISE = "supervise"
    const val TAG_POSITION = "position"
    const val TAG_DIRECTION = "direction"
    const val TAG_DISTANCE = "distance"
    const val TAG_LOCATION = "location"
    const val TAG_SURVEY = "survey"
    const val TAG_PREVIEW = "preview"
    const val TAG_PHOTO = "photo"
    val PIPE_SHAPES = arrayOf("직진형", "T분기형", "엘보형", "관말형")
    val PIPE_DIRECTIONS = arrayOf("", "", "out", "", "outl", "", "outr", "", "in", "")
    val PIPE_TYPE_ENUMS = PipeTypeEnum.values()

    internal const val REQUEST_MAP = 30001
    internal const val REQUEST_CAPTURE_IMAGE = 10001
    internal const val REQUEST_GALLERY = 10002

    const val RESULT_PASS = 200
    const val RESULT_FAIL = 400
}
