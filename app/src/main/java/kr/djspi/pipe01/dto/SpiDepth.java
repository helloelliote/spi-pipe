package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiDepth extends SpiData {

    private int id;
    /**
     * 관로 심도(단위 m)
     */
    private double depth;
}
