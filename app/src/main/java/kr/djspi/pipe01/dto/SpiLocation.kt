package kr.djspi.pipe01.dto

import java.io.Serializable

data class SpiLocation(var id: Int = -1) : DataItem, Serializable {

    var spi_id: Int? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var count: Int = 0
        set(value) {
            field = value
            field++
        }
}
