package kr.djspi.pipe01.dto;

import java.io.Serializable;

import kr.djspi.pipe01.R;
import lombok.Data;
import lombok.Getter;

@Data
public class PipeType implements PipeData {

    private int id;
    static final String HEAD_NULL = "";
    static final String HEAD_RAD = "관경  ";
    static final String UNIT_MM = "  mm";
    static final String UNIT_CORE = "  코어";

    @Getter
    public enum PipeTypeEnum implements Serializable {
        Pipe_City(R.string.pipe_name_00, R.drawable.cir_01_map, 1, HEAD_RAD, UNIT_MM),
        Pipe_Water(R.string.pipe_name_01, R.drawable.cir_02_map, 1, HEAD_RAD, UNIT_MM),
        Pipe_Drain(R.string.pipe_name_02, R.drawable.cir_03_map, 1, HEAD_RAD, UNIT_MM),
        Pipe_Sewer(R.string.pipe_name_03, R.drawable.cir_04_map, 1, HEAD_RAD, UNIT_MM),
        Pipe_Electric(R.string.pipe_name_04, R.drawable.cir_05_map, 2, HEAD_RAD, UNIT_MM),
        Pipe_Communication(R.string.pipe_name_05, R.drawable.cir_06_map, 3, HEAD_NULL, UNIT_CORE),
        Pipe_heating(R.string.pipe_name_06, R.drawable.cir_07_map, 4, HEAD_RAD, UNIT_MM),
        Pipe_Oil(R.string.pipe_name_07, R.drawable.cir_08_map, 1, HEAD_RAD, UNIT_MM),
        Pipe_Lamp(R.string.pipe_name_08, R.drawable.cir_09_map, 5, HEAD_RAD, UNIT_MM),
        Pipe_Cctv(R.string.pipe_name_09, R.drawable.cir_10_map, 5, HEAD_RAD, UNIT_MM),
        Pipe_Traffic(R.string.pipe_name_10, R.drawable.cir_11_map, 5, HEAD_RAD, UNIT_MM),
        Pipe_Etc(R.string.pipe_name_11, R.drawable.cir_12_map, 5, HEAD_RAD, UNIT_MM);

        private int nameRes;
        private int drawRes;
        private int type;
        private String header;
        private String unit;

        PipeTypeEnum(int name, int draw, int type, String header, String unit) {
            this.nameRes = name;
            this.drawRes = draw;
            this.type = type;
            this.header = header;
            this.unit = unit;
        }
    }
}
