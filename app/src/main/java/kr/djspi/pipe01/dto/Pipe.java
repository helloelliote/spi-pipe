package kr.djspi.pipe01.dto;

import java.io.Serializable;

import kr.djspi.pipe01.R;

public enum Pipe implements Serializable {
    Pipe_City(R.string.pipe_name_00, R.drawable.cir_01_map, 1),
    Pipe_Water(R.string.pipe_name_01, R.drawable.cir_02_map, 1),
    Pipe_Drain(R.string.pipe_name_02, R.drawable.cir_03_map, 1),
    Pipe_Sewer(R.string.pipe_name_03, R.drawable.cir_04_map, 1),
    Pipe_Electric(R.string.pipe_name_04, R.drawable.cir_05_map, 2),
    Pipe_Communication(R.string.pipe_name_05, R.drawable.cir_06_map, 3),
    Pipe_heating(R.string.pipe_name_06, R.drawable.cir_07_map, 4),
    Pipe_Oil(R.string.pipe_name_07, R.drawable.cir_08_map, 1),
    Pipe_Lamp(R.string.pipe_name_08, R.drawable.cir_09_map, 5),
    Pipe_Cctv(R.string.pipe_name_09, R.drawable.cir_10_map, 5),
    Pipe_Traffic(R.string.pipe_name_10, R.drawable.cir_11_map, 5),
    Pipe_Etc(R.string.pipe_name_11, R.drawable.cir_12_map, 5);

    private int nameRes;
    private int drawRes;
    private int type;

    Pipe(int name, int draw, int type) {
        this.nameRes = name;
        this.drawRes = draw;
        this.type = type;
    }
}
