package kr.djspi.pipe01.dto

import com.google.gson.JsonObject
import java.io.Serializable

/**
 * (중요) DTO 클래스는 Proguard 난독화 대상에서 제외시켜야 Json 파싱 과정에서 각종 변수명들이 유지된다.
 * proguard-rules.pro: -keep class kr.djspi.pipe01.dto.** { *; }
 */
class Entry(
    val spi: Spi,
    val spi_type: SpiType,
    val spi_memo: SpiMemo,
    val spi_photo: SpiPhoto,
    val pipe: Pipe,
    val pipe_type: PipeType,
    val pipe_shape: PipeShape,
    val pipe_position: PipePosition,
    val pipe_plan: PipePlan,
    val pipe_supervise: PipeSupervise
) : Serializable {

    var spi_location: SpiLocation? = null

    companion object {

        fun parseEntry(entries: ArrayList<*>, index: Int, vararg strings: String?): JsonObject {
            return parseEntrySingle((entries[index] as Entry), strings)
        }

        private fun parseEntrySingle(entry: Entry, strings: Array<out String?>): JsonObject {
            return JsonObject().apply {
                addProperty("spi_id", entry.spi.id)
                addProperty("supervise", entry.pipe_supervise.supervise)
                addProperty("supervise_contact", entry.pipe.supervise_contact)
                addProperty("construction", entry.pipe.construction)
                addProperty("construction_contact", entry.pipe.construction_contact)
                addProperty("spi_type", entry.spi_type.type)
                addProperty("pipe", entry.pipe_type.pipe)
                addProperty("shape", entry.pipe_shape.shape)
                addProperty("header", entry.pipe_type.header)
                addProperty("spec", entry.pipe_shape.spec)
                addProperty("unit", entry.pipe_type.unit)
                addProperty("material", entry.pipe.material)
                addProperty("position", entry.pipe_position.position)
                addProperty("direction", entry.pipe_position.direction)
                addProperty("horizontal", entry.pipe_position.horizontal)
                addProperty("vertical", entry.pipe_position.vertical)
                addProperty("horizontal_form", strings[0])
                addProperty("vertical_form", strings[1])
                addProperty("depth", entry.pipe.depth)
                addProperty("spi_memo", entry.spi_memo.memo)
                addProperty("file_plane", entry.pipe_plan.file_plane)
                addProperty("file_section", entry.pipe_plan.file_section)
                addProperty("latitude", entry.spi_location!!.latitude)
                addProperty("longitude", entry.spi_location!!.longitude)
            }
        }
    }
}
