package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Spi implements DataItem, Serializable {

    /**
     * 고유 id 값과 serial 은 태깅과 동시에 바로 생성해야 한다.
     */
    private int id;
    private final String serial;
    private final int type_id;
    private boolean hidden = false;
}
