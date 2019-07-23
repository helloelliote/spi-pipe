package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpiPhoto implements DataItem, Serializable {

    public int id = -1;
    public int spi_id;
    /**
     * (선택입력) 현장 사진이 업로드 된 주소
     */
    public String url;
}
