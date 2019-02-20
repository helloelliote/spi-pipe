package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiMemo implements SpiData {

    private int id;
    /**
     * (선택입력) 사용자 메모 입력: 글자수 제한 필요
     */
    private String memo;
}
