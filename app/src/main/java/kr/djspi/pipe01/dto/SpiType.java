package kr.djspi.pipe01.dto;

import com.google.gson.JsonElement;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpiType extends JsonElement implements SpiData, Serializable {

    private int id;
    /**
     * SPI 제품 종류: 표지기, 표지판, 표지주
     */
    private String type;

    @Override
    public JsonElement deepCopy() {
        return null;
    }
}
