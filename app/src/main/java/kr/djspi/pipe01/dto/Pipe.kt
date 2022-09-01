package kr.djspi.pipe01.dto

import java.io.Serializable

data class Pipe(var id: Int = -1) : DataItem, Serializable {

    var type_id: Int? = null
    var spi_id: Int? = null
    var depth: Double? = null
    var material: String? = null
    var supervise_id: Int? = null
    var supervise_contact: String? = null
    var construction: String? = null
    var construction_contact: String? = null
}
