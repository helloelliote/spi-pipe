package kr.djspi.pipe01.dto;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.NonNull;

public class Spi implements PipeObject, SpiData {

    private int id;
    private int type_id;
    private String serial;
    private boolean isHidden = false;
    private ArrayList<PipeObject> pipeList = new ArrayList<>();
    private ArrayList<SpiData> spiDataList = new ArrayList<>();

    public Spi(@NonNull int id) {
        this.id = id;
        pipeList.clear();
        spiDataList.clear();
    }

    public void addSpiData(SpiData spiData) {
        spiData.setId(id);
        spiDataList.add(spiData);
    }

    public void removeSpiData(SpiData spiData) {
        spiDataList.remove(spiData);
    }

    public void addPipe(PipeObject pipe) {
        pipe.setId(id);
        pipeList.add(pipe);
    }

    public void removePipe(PipeObject pipe) {
        pipeList.remove(pipe);
    }


    public Spi setTypeId(@NonNull int type_id) {
        this.type_id = type_id;
        return this;
    }

    public Spi setSerial(@NonNull String serial) {
        this.serial = serial;
        return this;
    }

    public void setId(int id)  {
        System.err.println("Do not call setId() separately, use constructor to properly set id to: " + id + ".");
    }

    @NotNull
    public String toString() {
        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("request", "spi-set");
        hashMap.put("data", this);
        return new Gson().toJson(hashMap);
    }
}
