package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Spi implements DataItem, Serializable {

    public Spi(String serial, int type_id) {
        this.serial = serial;
        this.type_id = type_id;
    }
    /**
     * 고유 id 값과 serial 은 태깅과 동시에 바로 생성해야 한다.
     */
    public int id;
    public final String serial;
    public final int type_id;
    public boolean hidden = false;
}
