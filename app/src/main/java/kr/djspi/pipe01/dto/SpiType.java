package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;

@Data
public class SpiType implements DataItem, Serializable {

    private final int id;
    /**
     * SPI 제품 종류: 표지기, 표지판, 표지주
     */
    private final String type;

    @Getter
    public enum SpiTypeEnum {

        표지판("plate"),
        표지기("marker"),
        표지주("column");

        private String code;

        SpiTypeEnum(String code) {
            this.code = code;
        }
    }
}
