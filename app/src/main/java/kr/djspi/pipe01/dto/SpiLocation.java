package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiLocation implements DataItem {

    private int id;
    private int spi_id;
    /**
     * 위도(latitude), 경도(longitude) 좌표
     */
    private double latitude;
    private double longitude;
    /**
     * count 값은 사용자가 직접 입력하지 않는 값
     */
    private int count;
}
