package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipeShape implements DataItem {

    /**
     * '관로형태(shape)' 와 방향, 스펙 정보를 함께 다루는 클래스
     * T 분기형 관로의 경우 본관, 지관 구경이 달라질 수 있어 spec_sub 를 두고 사용한다.
     */
    private int id = -1;
    private int pipe_id;
    private String shape;
    private String spec;
    private String spec_sub;
}
