package kr.djspi.pipe01.dto

import java.io.Serializable

data class PipePosition(var id: Int = -1) : DataItem, Serializable {

    /**
     * 관로를 추가할 때 받아야 하는 대상 관로의 고유 id 값
     */
    var pipe_id: Int = -1
    /**
     * 관로의 위치를 다이얼 키패드 기준으로, 왼쪽위 부터 시작해 1~9 까지 배정
     * (관로가 SPI 바로 아래 위치할 경우 5)
     * <p>
     * 관로의 분기방향은 동서남북 방위를 이용해 표현: sw, ne, e
     */
    var position: Int = -1
    var direction: String? = null
    /**
     * 수직, 수평 거리(단위 m)
     */
    var vertical: Double = -1.0
    var horizontal: Double = -1.0
}
