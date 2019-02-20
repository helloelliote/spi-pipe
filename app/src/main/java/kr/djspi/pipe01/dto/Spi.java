package kr.djspi.pipe01.dto;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Data;

@Data
public class Spi implements ListInterface, SpiData {

    private int id;
    private String serial;
    private boolean isHidden = false;
//    private String spiType; // 표지판, 표지기, 표지주
    private ArrayList<Pipe> pipeList = new ArrayList<>();
    private ArrayList<SpiData> attrList = new ArrayList<>();

    public Spi(int id) {
        this.id = id;
        pipeList.clear();
        attrList.clear();
    }

    public void addSpiData(SpiData spiData) {
        spiData.setId(id);
        attrList.add(spiData);
    }

    public void removeSpiData(SpiData spiData) {
        attrList.remove(spiData);
    }


    public void addPipe(Pipe pipe) {
        pipeList.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        pipeList.remove(pipe);
    }

    @NotNull
    public String toString() {
        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("request", "spi-set");
        hashMap.put("data", this);
        return new Gson().toJson(hashMap);
    }
}
