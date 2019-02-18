package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiPosition extends SpiData {

    private int id;
    private int position;
    private int pVertical;
    private int pHorizontal;
}
