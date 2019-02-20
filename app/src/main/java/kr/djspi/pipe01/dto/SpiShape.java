package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiShape extends SpiData {

    /**
     * '관로형태(shape)' 와 '방향(direction)' 정보를 함께 다루는 클래스(=테이블)
     * 두 자료를 어떤 자료형으로 입력받을 것인지 논의 필요
     */
    private int id;
    private String shape;
    private int direction;
//    private String direction;
}
