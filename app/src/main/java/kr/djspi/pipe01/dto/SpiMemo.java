package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpiMemo implements DataItem, Serializable {

    /**
     * (선택입력) 사용자 메모 입력: 글자수 제한 필요
     */
    private final String memo;
    private int id = -1;
    private int spi_id = -1;
}
