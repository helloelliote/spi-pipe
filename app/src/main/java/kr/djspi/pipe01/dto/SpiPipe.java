package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiPipe extends SpiData {

    private int id;
    private static Pipe[] pipes;
    private String title;
    private String spec;
    private String unit;

    public SpiPipe() {
        pipes = Pipe.values();
    }
}
