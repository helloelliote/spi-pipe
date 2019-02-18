package kr.djspi.pipe01.dto;

import com.google.gson.JsonElement;

import lombok.Data;

@Data
public class SpiSupervise extends SpiData {

    private int id;
    private String supervise;
    private String contact;
}
