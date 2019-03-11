package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpiLocation implements DataItem, Serializable {

    private int id = -1;
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

    public void setCount(int i) {
        this.count = i;
        this.count++;
    }
}
