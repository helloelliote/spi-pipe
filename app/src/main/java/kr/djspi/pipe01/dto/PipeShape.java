package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;

@Data
public class PipeShape implements DataItem, Serializable {

    /**
     * '관로형태(shape)' 와 방향, 스펙 정보를 함께 다루는 클래스
     * T 분기형 관로의 경우 본관, 지관 구경이 달라질 수 있어 spec_sub 를 두고 사용한다.
     */
    private int id = -1;
    private int pipe_id = -1;
    private String shape;
    private String spec;
    private String spec_sub;

    @Getter
    @SuppressWarnings("NonAsciiCharacters")
    public enum PipeShapeEnum {

        직진형("직진형", "str"),
        T분기형("T분기형", "tbr"),
        엘보형("엘보형", "elb"),
        관말형("관말형", "end");

        private String name;
        private String code;

        PipeShapeEnum(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public static String parsePipeShape(String pipeShape) {
            for (PipeShapeEnum shapeEnum : PipeShapeEnum.values()) {
                if (shapeEnum.name.equals(pipeShape)) {
                    return shapeEnum.code;
                }
            }
            return null;
        }
    }
}
