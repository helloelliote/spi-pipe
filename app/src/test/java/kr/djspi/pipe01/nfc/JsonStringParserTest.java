package kr.djspi.pipe01.nfc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.ArrayList;

import kr.djspi.pipe01.dto.Entry;

public class JsonStringParserTest {

    String[] strings = new String[10];

    @Test
    public void doTest(ArrayList<Entry> arrayList, int index) {
        String json = new Gson().toJson(arrayList);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    }

}