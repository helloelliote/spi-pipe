package kr.djspi.pipe01.nfc;

import com.google.gson.JsonObject;
import com.helloelliote.json.Json;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public enum StringParser {
    ID("spi_id", "ID:"),
    PIPE("pipe", ""),
    SHAPE("shape", ""),
    MATERIAL("material", ""),
    HEADER("header", ""),
    SPEC("spec", ""),
    UNIT("unit", ""),
    VERTICAL("vertical", "수직m:"),
    HORIZONTAL("horizontal", "수평m:"),
    DEPTH("depth", "심도m:"),
    SUPERVISE("supervise", "관리처:"),
    CONTACT("supervise_contact", "");

    private String memberName;
    private String memberLabel;

    StringParser(String memberName, String memberLabel) {
        this.memberName = memberName;
        this.memberLabel = memberLabel;
    }

    public static String[] parseToStringArray(@NotNull JsonObject jsonObject, int index) {
        final StringParser[] parsers = StringParser.values();
        final int length = parsers.length;
        JsonObject object = Json.o(jsonObject, "data", index);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(parsers[i].memberLabel).append(Json.s(object, parsers[i].memberName)).append(" ");
        }
        String[] strings = new String[1];
        strings[0] = builder.toString().trim();
        return strings;
    }
}
