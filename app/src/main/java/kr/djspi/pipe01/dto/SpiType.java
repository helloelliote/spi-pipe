package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpiType implements SpiData, Serializable {

    private int id;
    /**
     * SPI 제품 종류: 표지기, 표지판, 표지주
     */
    private String type;
}
