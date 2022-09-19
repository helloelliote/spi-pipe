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
    @Suppress("NonAsciiCharacters", "EnumEntryName")
    enum class PipeTypeEnum(
        internal val pipeName: String,
        internal val drawRes: Int,
        internal val drawResValve: Int,
        internal val header: String,
        internal val unit: String
    ) : Serializable {
        가스관로("가스관로", R.drawable.cir_01_map, R.drawable.cir_01_map_valve, "관경", "mm"),
        상수관로("상수관로", R.drawable.cir_02_map, R.drawable.cir_02_map_valve, "관경", "mm"),
        하수관로("하수관로", R.drawable.cir_03_map, R.drawable.cir_03_map_valve, "관경", "mm"),
        오수관로("오수관로", R.drawable.cir_04_map, R.drawable.cir_04_map_valve, "관경", "mm"),
        전기관로("전기관로", R.drawable.cir_05_map, R.drawable.cir_05_map_valve, "관경", ""),
        통신관로("통신관로", R.drawable.cir_06_map, R.drawable.cir_06_map_valve, "관로수", ""),
        난방관로("난방관로", R.drawable.cir_07_map, R.drawable.cir_07_map_valve, "관경", "mm"),
        유류관로("유류관로", R.drawable.cir_08_map, R.drawable.cir_08_map_valve, "관경", "mm"),
        가로등("가로등", R.drawable.cir_09_map, R.drawable.cir_09_map_valve, "코어수", "코어"),
        CCTV("CCTV", R.drawable.cir_10_map, R.drawable.cir_10_map_valve, "코어수", "코어"),
        광케이블("광케이블", R.drawable.cir_11_map, R.drawable.cir_11_map_valve, "관로수", ""),
        기타관로("기타관로", R.drawable.cir_12_map, R.drawable.cir_12_map_valve, "관경", "mm"),
        우수관로("우수관로", R.drawable.cir_12_map, R.drawable.cir_12_map_valve, "관경", "mm");

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
