package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipeShape implements DataItem {

    /**
     * '관로형태(shape)' 와 '방향(direction)' 정보를 함께 다루는 클래스(=테이블)
     * 두 자료를 어떤 자료형으로 입력받을 것인지 논의 필요
     */
    private int id;
    private int pipe_id;
    private int direction;
    private String shape;
//    private String direction;
}
