package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiPhoto implements DataItem {

    private final int spi_id;
    private int id = -1;
    /**
     * (선택입력) 현장 사진이 업로드 된 주소
     */
    private String url;
}
