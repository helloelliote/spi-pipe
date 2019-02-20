package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiType extends SpiData {

    private int id;
    /**
     * SPI 제품 종류: 표지기, 표지판, 표지주
     */
    private String type;
}
