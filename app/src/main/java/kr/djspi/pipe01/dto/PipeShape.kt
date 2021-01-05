package kr.djspi.pipe01.dto

import java.io.Serializable

@Suppress("NonAsciiCharacters")
data class PipeShape(var id: Int = -1) : DataItem, Serializable {

    var pipe_id: Int = -1
    var shape: String? = null
    var spec: String? = null
    var spec_sub: String? = null

    /**
     * 새 관로형태를 목록에 추가하려면 아래의 Enum 에 항목을 추가해준다 (추가 수정 불필요)
     */
    @Suppress("NonAsciiCharacters", "EnumEntryName", "unused")
    enum class PipeShapeEnum(internal var type: String, internal var code: String) {

        직진형("직진형", "str"),
        T분기형("T분기형", "tbr"),
        엘보형("엘보형", "elb"),
        엘보형135("엘보형(135º)", "elb135"),
        관말형("관말형", "end"),
        선택형("선택형", "none");

        companion object {
            fun parsePipeShape(pipeShape: String?): String? {
                values().forEach {
                    if (it.type == pipeShape) {
                        return it.code
                    }
                }
                return null
            }

            fun getSelectableTypes(): List<String> {
                val array = mutableListOf<String>()
                for (i in 0..values().size - 2) {
                    array.add(i, values()[i].type)
                }
                return array
            }
        }
    }
}
