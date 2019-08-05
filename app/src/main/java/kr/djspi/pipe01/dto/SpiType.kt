package kr.djspi.pipe01.dto

import java.io.Serializable

/**
 * SPI 제품 종류: 표지기, 표지판, 표지주
 */
class SpiType(val id: Int = -1, val type: String) : DataItem, Serializable {

    @Suppress("NonAsciiCharacters", "EnumEntryName", "unused")
    enum class SpiTypeEnum(internal var typeName: String, internal var code: String) {

        표지판("표지판", "plate"),
        표지기("표지기", "marker"),
        표지주("표지주", "column");

        companion object {

            fun parseSpiType(spiType: String?): String? {
                values().forEach {
                    if (it.typeName == spiType) {
                        return it.code
                    }
                }
                return null
            }
        }
    }
}
