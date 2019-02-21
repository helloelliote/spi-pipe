package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipePosition implements PipeData {

    private int id;
    private int pipe_id;
    /**
     *  관로의 위치를 다이얼 키패드 기준으로, 왼쪽위 부터 시작해 1~9 까지 배정
     *  (관로가 SPI 바로 아래 위치할 경우 5)
     */
    private int position;
    /**
     * 수직, 수평 거리(단위 m)
     */
    private double vertical;
    private double horizontal;
}
