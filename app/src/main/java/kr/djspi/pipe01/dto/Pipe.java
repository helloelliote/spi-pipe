package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class Pipe implements DataItem {

    private int id = -1;
    private int type_id;
    private int supervise_id;
    private int construction_id;
    private final int spi_id;
    private final double depth;
    private final int spec;
    private final String material;
}
