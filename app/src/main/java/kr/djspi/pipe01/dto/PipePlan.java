package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipePlan implements PipeData {

    private int id;
    /**
     * 평면도(plane) 와 단면도(section) 도면 정보: 아래의 사용자 입력값에 따라 변화되는 값
     * @see SpiType#type SPI 제품 타입(표지판, 표지기, 표지주) 에 따라 단면도 변화
     * @see PipeShape#shape 관로 형태(T분기형, 직진형 등) 에 따라 평면도 변화
     * @see PipeShape#direction 관로 분기 방향에 따라 평면도 변화
     */
    private String plane;
    private String section;
}
