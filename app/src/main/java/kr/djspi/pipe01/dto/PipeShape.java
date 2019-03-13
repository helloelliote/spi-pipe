package kr.djspi.pipe01.dto;

import java.io.Serializable;

import kr.djspi.pipe01.R;
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
    public enum PipeShapeEnum {

        직진형(R.string.shape_name_00, "str"),
        T분기형(R.string.shape_name_01, "tbr"),
        엘보형(R.string.shape_name_02, "elb"),
        관말형(R.string.shape_name_03, "end");

        private int name;
        private String code;

        PipeShapeEnum(int name, String code) {
            this.name = name;
            this.code = code;
        }
    }
}
