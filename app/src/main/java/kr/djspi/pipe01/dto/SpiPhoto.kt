package kr.djspi.pipe01.dto

import java.io.Serializable

data class SpiPhoto(var id: Int = -1) : DataItem, Serializable {

    var spi_id: Int? = null
    var url: String? = null
}
