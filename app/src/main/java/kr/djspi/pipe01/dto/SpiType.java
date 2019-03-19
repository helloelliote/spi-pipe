package kr.djspi.pipe01.dto;

import org.jetbrains.annotations.Nullable;

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
    @SuppressWarnings("NonAsciiCharacters")
    public enum SpiTypeEnum {

        표지판("표지판", "plate"),
        표지기("표지기", "marker"),
        표지주("표지주", "column");

        private String name;
        private String code;

        SpiTypeEnum(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @Nullable
        public static String parseSpiType(String spiType) {
            for (SpiTypeEnum typeEnum : SpiTypeEnum.values()) {
                if (typeEnum.name.equals(spiType)) {
                    return typeEnum.getCode();
                }
            }
            return null;
        }
    }
}
