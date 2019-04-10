package kr.djspi.pipe01.nfc;

import com.google.gson.JsonObject;
import com.helloelliote.util.json.Json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static kr.djspi.pipe01.dto.PipeShape.PipeShapeEnum.parsePipeShape;
import static kr.djspi.pipe01.dto.SpiType.SpiTypeEnum.parseSpiType;

public enum StringParser {
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

    private String name;
    private String label;

    StringParser(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public static String[] parseToStringArray(@NotNull JsonObject jsonObject, int index) {
        final StringParser[] parsers = StringParser.values();
        final int length = parsers.length;
        JsonObject dataObject = Json.o(jsonObject, "data", index);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(parsers[i].label).append(Json.s(dataObject, parsers[i].name)).append(" ");
        }
        String[] strings = new String[1];
        strings[index] = builder.toString().trim();
        return strings;
    }

    public static JsonObject parseToJsonObject(@NotNull ArrayList<String> stringArrayList, int index) {
        final StringParser[] parsers = StringParser.values();
        final int length = parsers.length;
        String dataString = stringArrayList.get(index)
                .replace(POSITION.label, "")
                .replace(VERTICAL.label, "")
                .replace(HORIZONTAL.label, "")
                .replace(DEPTH.label, "")
                .replace(SUPERVISE.label, "");
        String[] splitDataString = dataString.split(" ", length);
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < length; i++) {
            jsonObject.addProperty(parsers[i].name, splitDataString[i]);
        }
        jsonObject.addProperty("file_plane", parsePlanePlan(jsonObject));
        jsonObject.addProperty("file_section", parseSectionPlan(jsonObject));
        return jsonObject;
    }

    @NotNull
    private static String parsePlanePlan(JsonObject jsonObject) {
        return String.format("plan_%s_%s_%s_%s_distance",
                parseSpiType(Json.s(jsonObject, TYPE.name)),
                parsePipeShape(Json.s(jsonObject, SHAPE.name)),
                Json.s(jsonObject, POSITION.name),
                Json.s(jsonObject, DIRECTION.name));
    }

    @NotNull
    private static String parseSectionPlan(JsonObject jsonObject) {
        return String.format("plan_%s_%s",
                parseSpiType(Json.s(jsonObject, TYPE.name)),
                Json.s(jsonObject, "position"));
    }
}
