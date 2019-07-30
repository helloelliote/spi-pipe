package kr.djspi.pipe01.dto

import java.io.Serializable

data class PipePlan(var id: Int = -1) : DataItem, Serializable {

    var pipe_id: Int = -1
    /**
     * 평면도(plane) 와 단면도(section) 도면 정보: 아래의 사용자 입력값에 따라 변화되는 값
     */
    var file_plane: String? = null
    var file_section: String? = null
}
