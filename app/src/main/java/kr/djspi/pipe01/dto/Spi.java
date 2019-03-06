package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.NonNull;

public class Spi extends JsonElement implements SpiData, Serializable {

    private int id;
    private int type_id;
    private String serial;
    private boolean isHidden = false;
    private ArrayList<Pipe> pipeList = new ArrayList<>();
    private HashMap<String, SpiData> hashMap = new HashMap<>();

    public Spi(@NonNull int id) {
        this.id = id;
        pipeList.clear();
        hashMap.clear();
    }

    public void addSpiData(SpiData spiData) {
        hashMap.put(spiData.getClass().getSimpleName(), spiData);
    }

    public void removeSpiData(SpiData spiData) {
        hashMap.remove(spiData);
    }

    public void addPipe(Pipe pipe) {
        pipeList.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        pipeList.remove(pipe);
    }

    public void setTypeId(@NonNull int type_id) {
        this.type_id = type_id;
    }

    public void setSerial(@NonNull String serial) {
        this.serial = serial;
    }

    @Override
    public JsonElement deepCopy() {
        return null;
    }

    @NotNull
    public String toString() {
        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("request", "spi-set");
        hashMap.put("data", this);
        return new Gson().toJson(hashMap);
    }

    @Override
    public void setId(int id) {

    }

    public int getId() {
        return id;
    }

    public SpiData getSpiData(String key) {
        return hashMap.get(key);
    }
}
