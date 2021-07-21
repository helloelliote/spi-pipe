package kr.djspi.pipe01.dto

import kr.djspi.pipe01.R
import java.io.Serializable

data class PipeType(var id: Int = -1) : DataItem, Serializable {

    var header: String? = null
    var pipe: String? = null
    var unit: String? = null

    /**
     * Enum 목록의 순서는 변경하지 않는다.
     */
    @Suppress("NonAsciiCharacters", "EnumEntryName", "unused")
    enum class PipeTypeEnum(
        internal val pipeName: String,
        internal val drawRes: Int,
        internal val header: String,
        internal val unit: String
    ) : Serializable {
        도시가스("도시가스", R.drawable.cir_01_map, "관경", "mm"),
        상수관로("상수관로", R.drawable.cir_02_map, "관경", "mm"),
        하수관로("하수관로", R.drawable.cir_03_map, "관경", "mm"),
        오수관로("오수관로", R.drawable.cir_04_map, "관경", "mm"),
        전기관로("전기관로", R.drawable.cir_05_map, "코어수", "코어"),
        통신관로("통신관로", R.drawable.cir_06_map, "관로수", ""),
        난방관로("난방관로", R.drawable.cir_07_map, "관경", "mm"),
        유류관로("유류관로", R.drawable.cir_08_map, "관경", "mm"),
        가로등("가로등", R.drawable.cir_09_map, "코어수", "코어"),
        CCTV("CCTV", R.drawable.cir_10_map, "코어수", "코어"),
        광케이블("광케이블", R.drawable.cir_11_map, "관로수", ""),
        기타관로("기타관로", R.drawable.cir_12_map, "관경", "mm"),
        우수관로("우수관로", R.drawable.cir_12_map, "관경", "mm");

        companion object {
            fun parsePipeType(name: String): PipeTypeEnum {
                values().forEach {
                    if (it.pipeName == name) return it
                }
                return 기타관로
            }
        }
    }
}
