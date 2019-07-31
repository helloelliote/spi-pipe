package kr.djspi.pipe01.dto

import java.io.Serializable

data class PipeShape(var id: Int = -1) : DataItem, Serializable {

    var pipe_id: Int = -1
    var shape: String? = null
    var spec: String? = null
    var spec_sub: String? = null

    enum class PipeShapeEnum(internal var type: String, internal var code: String) {

        직진형("직진형", "str"),
        T분기형("T분기형", "tbr"),
        엘보형("엘보형", "elb"),
        관말형("관말형", "end");

        companion object {
            fun parsePipeShape(pipeShape: String?): String? {
                values().forEach {
                    if (it.type == pipeShape) {
                        return it.code
                    }
                }
                return null
            }
        }
    }
}
