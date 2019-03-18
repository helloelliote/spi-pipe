package kr.djspi.pipe01.dto;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;

import kr.djspi.pipe01.R;
import lombok.Data;
import lombok.Getter;

import static kr.djspi.pipe01.BaseActivity.pipes;

@Data
public class PipeType implements DataItem, Serializable {

    static final String HEAD_NULL = "";
    static final String HEAD_RAD = "관경";
    static final String UNIT_MM = "mm";
    static final String UNIT_CORE = "코어";
    private int id;
    private String header;
    private String pipe;
    private String unit;

    @Contract(pure = true)
    public static PipeTypeEnum parsePipeType(String name) {
        for (PipeTypeEnum pipe : pipes) {
            if (pipe.name.equals(name)) return pipe;
        }
        return PipeTypeEnum.기타관로;
    }

    /**
     * Enum 목록의 순서는 변경하지 않는다.
     */
    @Getter
    @SuppressWarnings("NonAsciiCharacters")
    public enum PipeTypeEnum implements Serializable {
        도시가스("도시가스", R.drawable.cir_01_map, HEAD_RAD, UNIT_MM),
        상수관로("상수관로", R.drawable.cir_02_map, HEAD_RAD, UNIT_MM),
        하수관로("하수관로", R.drawable.cir_03_map, HEAD_RAD, UNIT_MM),
        오수관로("오수관로", R.drawable.cir_04_map, HEAD_RAD, UNIT_MM),
        전기관로("전기관로", R.drawable.cir_05_map, HEAD_RAD, UNIT_MM),
        통신관로("통신관로", R.drawable.cir_06_map, HEAD_NULL, UNIT_CORE),
        난방관로("난방관로", R.drawable.cir_07_map, HEAD_RAD, UNIT_MM),
        유류관로("유류관로", R.drawable.cir_08_map, HEAD_RAD, UNIT_MM),
        가로등("가로등", R.drawable.cir_09_map, HEAD_RAD, UNIT_MM),
        CCTV("CCTV", R.drawable.cir_10_map, HEAD_RAD, UNIT_MM),
        교통관로("교통관로", R.drawable.cir_11_map, HEAD_RAD, UNIT_MM),
        기타관로("기타관로", R.drawable.cir_12_map, HEAD_RAD, UNIT_MM);

        private String name;
        private int drawRes;
        private String header;
        private String unit;

        PipeTypeEnum(String name, int drawRes, String header, String unit) {
            this.name = name;
            this.drawRes = drawRes;
            this.header = header;
            this.unit = unit;
        }
    }
}
