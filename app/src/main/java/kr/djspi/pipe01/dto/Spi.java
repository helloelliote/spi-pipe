package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Spi implements DataItem, Serializable {

    private final int id;
    private final String serial;
    private int type_id;
    private boolean hidden = false;
}
