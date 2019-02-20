package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiSerial extends SpiData {

    private int id;
    /**
     * NFC 칩 시리얼 번호: 현재 SPI 제품이 '초기화' 상태인지 '입력완료' 상태인지 등을 조회
     */
    private String serial;
}
