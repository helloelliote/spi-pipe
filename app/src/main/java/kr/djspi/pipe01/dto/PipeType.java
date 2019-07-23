package kr.djspi.pipe01.dto;

import java.io.Serializable;

import kr.djspi.pipe01.R;
import lombok.Data;

@Data
public class PipeType implements DataItem, Serializable {

    public int id;
    public String header;
    public String pipe;
    public String unit;

    public static PipeTypeEnum parsePipeType(String name) {
        for (PipeTypeEnum pipe : PipeTypeEnum.values()) {
            if (pipe.name.equals(name)) return pipe;
        }
        return PipeTypeEnum.기타관로;
    }

    static final String HEAD_NULL = "";
    static final String HEAD_RAD = "관경";
    static final String HEAD_CORE = "코어수";
    static final String UNIT_MM = "mm";
    static final String UNIT_CORE = "코어";

    /**
     * Enum 목록의 순서는 변경하지 않는다.
     */
    @SuppressWarnings("NonAsciiCharacters")
    public enum PipeTypeEnum implements Serializable {
        도시가스("도시가스", R.drawable.cir_01_map, HEAD_RAD, UNIT_MM),
        상수관로("상수관로", R.drawable.cir_02_map, HEAD_RAD, UNIT_MM),
        하수관로("하수관로", R.drawable.cir_03_map, HEAD_RAD, UNIT_MM),
        오수관로("오수관로", R.drawable.cir_04_map, HEAD_RAD, UNIT_MM),
        전기관로("전기관로", R.drawable.cir_05_map, HEAD_CORE, UNIT_CORE),
        통신관로("통신관로", R.drawable.cir_06_map, HEAD_CORE, UNIT_CORE),
        난방관로("난방관로", R.drawable.cir_07_map, HEAD_RAD, UNIT_MM),
        유류관로("유류관로", R.drawable.cir_08_map, HEAD_RAD, UNIT_MM),
        가로등("가로등", R.drawable.cir_09_map, HEAD_CORE, UNIT_CORE),
        CCTV("CCTV", R.drawable.cir_10_map, HEAD_CORE, UNIT_CORE),
        광케이블("광케이블", R.drawable.cir_11_map, HEAD_CORE, UNIT_CORE),
        기타관로("기타관로", R.drawable.cir_12_map, HEAD_RAD, UNIT_MM);

        private String name;
        private int drawRes;
        private String header;
        private String unit;

        public String getName() {
            return name;
        }

        public int getDrawRes() {
            return drawRes;
        }

        public String getHeader() {
            return header;
        }

        public String getUnit() {
            return unit;
        }

        PipeTypeEnum(String name, int drawRes, String header, String unit) {
            this.name = name;
            this.drawRes = drawRes;
            this.header = header;
            this.unit = unit;
        }
    }
}
