package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Spi extends SpiData {

    private int id;
    private static final int DATA_SIZE = 3;
    private ArrayList<SpiData> data = new ArrayList<>(DATA_SIZE);
    private ArrayList<Spi> spiArrayList;
    private JsonObject jsonObject;

    public Spi(int id) {
        this.id = id;
        data.clear();
    }

    public Spi add(SpiData spiData) {
        spiData.setId(id);
        data.add(spiData);
        return this;
    }

//    public boolean isDataValid() {
//        boolean isValid = false;
//
//        return isValid;
//    }

    public void remove(SpiData spiData) {
        data.remove(spiData);
    }

    public void clearData() {
        data.clear();
    }

    @NotNull
    public String toString() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(gson.toJson(data));
        jsonObject.add("data", jsonArray);
        return jsonObject.get("data").getAsString();
    }

//    public ArrayList<Spi> getArrayList() {
//        JsonArray jsonElements = new JsonArray();
//        jsonElements = jsonObject.get("data").getAsJsonArray();
//        for (JsonElement jsonElement : jsonElements) {
//            String jsonString = jsonElement.toString();
//            spiArrayList.add()
//        }
//        return spiArrayList;
//    }
}
