package kr.djspi.pipe01.dto

import java.io.Serializable

/**
 * enum 먼저 생성해서 서버에서 관리처 id (Pipe.class 의 supervise_id) 와 id 에 물린 관리처명을 받아온다.
 */
data class PipeSupervise(var id: Int? = null) : DataItem, Serializable {

    var supervise: String? = null
}
