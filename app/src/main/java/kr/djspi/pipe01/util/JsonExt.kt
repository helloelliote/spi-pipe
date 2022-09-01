package kr.djspi.pipe01.util

import com.google.gson.JsonObject
import kr.djspi.pipe01.BaseActivity.Companion.currentSerial
import kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS
import kr.djspi.pipe01.dto.*

@Throws(UnsupportedOperationException::class)
fun parseServerData(data: JsonObject, serial: String): HashMap<String, DataItem> {
    currentSerial = serial
    val pipeType = PipeType(-1)
    val pipeShape = PipeShape()
    val pipeSupervise= PipeSupervise(-1)
    try {
        // PipeType.class DTO
        val pipeTypeId = data["pipe_type_id"].asInt
        pipeType.id = pipeTypeId
        pipeType.header = PIPE_TYPE_ENUMS[pipeTypeId - 1].header
        pipeType.pipe = PIPE_TYPE_ENUMS[pipeTypeId - 1].pipeName
        pipeType.unit = PIPE_TYPE_ENUMS[pipeTypeId - 1].unit
        // PipeShape.class DTO
        pipeShape.shape = data["pipe_shape"].asString
        // PipeSupervise.class DTO
        val pipeSuperviseId = data["pipe_supervise_id"].asInt
        pipeSupervise.id = pipeSuperviseId
        pipeSupervise.supervise = data["pipe_supervise"].asString
    } catch (e: UnsupportedOperationException) {
        throw e
    }
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
    // PipeLocation.class DTO
    val pipeLocation = PipeLocation()
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
    val hashMap = HashMap<String, DataItem>()
    hashMap["Spi"] = spi
    hashMap["SpiType"] = spiType
    hashMap["PipeType"] = pipeType
    hashMap["PipeShape"] = pipeShape
    hashMap["PipeSupervise"] = pipeSupervise
    hashMap["SpiLocation"] = spiLocation
    hashMap["PipeLocation"] = pipeLocation
    hashMap["SpiMemo"] = spiMemo
    hashMap["SpiPhoto"] = spiPhoto
    return hashMap
}
