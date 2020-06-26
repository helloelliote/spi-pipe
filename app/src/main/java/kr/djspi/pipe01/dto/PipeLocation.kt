package kr.djspi.pipe01.dto

import java.io.Serializable

data class PipeLocation(var id: Int = -1) : DataItem, Serializable {

    /**
     * 관로를 추가할 때 받아야 하는 대상 관로의 고유 id 값
     */
    var pipe_id: Int = -1
    var latitude: Double? = null
    var longitude: Double? = null
    var coordinate_x: Double? = null
    var coordinate_y: Double? = null
    var origin: String? = null
    var count: Int = 0
        set(value) {
            field = value
            field++
        }
}
