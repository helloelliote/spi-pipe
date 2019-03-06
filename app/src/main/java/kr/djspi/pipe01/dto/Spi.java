package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NonNull;

@Data
public class Spi implements SpiData, Serializable {

    private int id;
    private int type_id;
    private String serial;
    private boolean hidden = false;

    public Spi(@NonNull int id) {
        this.id = id;
    }
}
