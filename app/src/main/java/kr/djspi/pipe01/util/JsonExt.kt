package kr.djspi.pipe01.util

import com.google.gson.JsonObject
import kr.djspi.pipe01.dto.*
import java.util.*

fun parseServerData(data: JsonObject, serial: String): HashMap<String, DataItem> {
    val hashMap = HashMap<String, DataItem>()
    // Spi.class DTO
    val spiId = data["spi_id"].asInt
    val typeId = data["spi_type_id"].asInt
    val spi = Spi(serial, typeId)
    spi.id = spiId
    // SpiType.class DTO
    val type = data["spi_type"].asString
    val spiType = SpiType(typeId, type)
    // SpiLocation.class DTO
    val spiLocation = SpiLocation()
    if (!data["spi_location_id"].isJsonNull) {
        val locationId = data["spi_location_id"].asInt
        val latitude = data["spi_latitude"].asDouble
        val longitude = data["spi_longitude"].asDouble
        val count = data["spi_count"].asInt
        spiLocation.id = locationId
        spiLocation.spi_id = spiId
        spiLocation.latitude = latitude
        spiLocation.longitude = longitude
        spiLocation.count = count
    }
    // SpiMemo.class DTO
    val spiMemo = SpiMemo()
    if (!data["spi_memo_id"].isJsonNull) {
        val memoId = data["spi_memo_id"].asInt
        val memo = data["spi_memo"].asString
        spiMemo.id = memoId
        spiMemo.spi_id = spiId
        spiMemo.memo = memo
    }
    // SpiPhoto.class DTO
    val spiPhoto = SpiPhoto()
    if (!data["spi_photo_id"].isJsonNull) {
        val photoId = data["spi_photo_id"].asInt
        val url = data["spi_photo_url"].asString
        spiPhoto.id = photoId
        spiPhoto.spi_id = spiId
        spiPhoto.url = url
    }
    hashMap["Spi"] = spi
    hashMap["SpiType"] = spiType
    hashMap["SpiLocation"] = spiLocation
    hashMap["SpiMemo"] = spiMemo
    hashMap["SpiPhoto"] = spiPhoto
    return hashMap
}
