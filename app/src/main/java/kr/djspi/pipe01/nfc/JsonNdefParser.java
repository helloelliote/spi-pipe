package kr.djspi.pipe01.nfc;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static kr.djspi.pipe01.Const.MEMBERS_DEFAULT;

public class JsonNdefParser implements Serializable {

    private static ArrayList<String> recordList;
    private static JsonObject jsonObject;

    public JsonNdefParser(@NotNull JsonObject jsonObject) {
        JsonNdefParser.jsonObject = jsonObject;
        JsonNdefParser.recordList = new ArrayList<>();
    }

    public String[] getRecordList() {
        ArrayList<String> members = new ArrayList<>(asList(MEMBERS_DEFAULT));
        for (String member : members) {
            recordList.add(jsonObject.get(member).getAsString());
        }
        return new String[1];
    }
}
