package kr.djspi.pipe01.dto

import java.io.Serializable

/**
 * 고유 id 값과 serial 은 태깅과 동시에 바로 생성해야 한다.
 */
data class Spi(val serial: String, val type_id: Int) : DataItem, Serializable {

    var id: Int = -1
    var hidden: Boolean = false
}
