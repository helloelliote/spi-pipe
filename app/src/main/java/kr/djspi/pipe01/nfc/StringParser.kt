package kr.djspi.pipe01.nfc

import com.google.gson.JsonObject
import kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.Companion.parsePipeShape
import kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.Companion.parseSpiType
import java.util.*

@Suppress("unused")
enum class StringParser(
    private val property: String,
    private val label: String
) {
    ID("spi_id", ""),
    TYPE("spi_type", ""),
    PIPE("pipe", ""),
    SHAPE("shape", ""),
    MATERIAL("material", ""),
    HEADER("header", ""),
    SPEC("spec", ""),
    UNIT("unit", ""),
    POSITION("position", "위치:"),
    DIRECTION("direction", ""),
    VERTICAL("vertical", "수직m:"),
    HORIZONTAL("horizontal", "수평m:"),
    DEPTH("depth", "심도m:"),
    SUPERVISE("supervise", "관리:"),
    CONTACT("supervise_contact", "");

    companion object {

        fun parseToStringArray(jsonObject: JsonObject, index: Int): Array<String?> {
            val parsers = values()
            val dataObject: JsonObject = if (jsonObject["data"] != null) {
                jsonObject["data"].asJsonObject
            } else {
                jsonObject
            }

            val builder = StringBuilder()
            for (parser in parsers) {
                builder.append(parser.label).append(dataObject[parser.property].asString)
                    .append(" ")
            }
            val strings = arrayOfNulls<String>(1)
            strings[index] = builder.toString().trim { it <= ' ' }
            return strings
        }

        fun parseToJsonObject(stringArrayList: ArrayList<String>, index: Int): JsonObject {
            val parsers = values()
            val length = parsers.size
            val dataString = stringArrayList[index]
                .replace(POSITION.label, "")
                .replace(VERTICAL.label, "")
                .replace(HORIZONTAL.label, "")
                .replace(DEPTH.label, "")
                .replace(SUPERVISE.label, "")
            val splitDataString =
                dataString.split(" ".toRegex(), length.coerceAtLeast(0)).toTypedArray()
            val jsonObject = JsonObject()
            for (i in 0 until length) {
                jsonObject.addProperty(parsers[i].property, splitDataString[i])
            }
            jsonObject.addProperty("file_plane", parsePlanePlan(jsonObject))
            jsonObject.addProperty("file_section", parseSectionPlan(jsonObject))
            return jsonObject
        }

        private fun parsePlanePlan(jsonObject: JsonObject): String {
            return String.format(
                "plan_%s_%s_%s_%s_distance",
                parseSpiType(jsonObject[TYPE.property].asString),
                parsePipeShape(jsonObject[SHAPE.property].asString),
                jsonObject[POSITION.property].asString,
                jsonObject[DIRECTION.property].asString
            )
        }

        private fun parseSectionPlan(jsonObject: JsonObject): String {
            return String.format(
                "plan_%s_%string",
                parseSpiType(jsonObject[TYPE.property].asString),
                jsonObject["position"].asString
            )
        }
    }
}
