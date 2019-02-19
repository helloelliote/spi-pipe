package kr.djspi.pipe01.dto;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Data;

@Data
public class Spi extends SpiData {

    private int id;
    private static final int DATA_SIZE = 3;
    private ArrayList<SpiData> attr = new ArrayList<>(DATA_SIZE);
    private ArrayList<Spi> spiArrayList;

    public Spi(int id) {
        this.id = id;
        attr.clear();
    }

    public void setData(SpiData spiData) {
        spiData.setId(id);
        attr.add(spiData);
    }

    public void removeData(SpiData spiData) {
        attr.remove(spiData);
    }

    @NotNull
    public String toString() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("request", "spi-set");
        hashMap.put("data", this);
        return new Gson().toJson(hashMap);
    }
}
