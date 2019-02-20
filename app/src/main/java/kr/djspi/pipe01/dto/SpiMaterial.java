package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiMaterial extends SpiData {

    private int id;
    /**
     * 관로 재질 (글자수 제한 필요)
     */
    private String material;
}
