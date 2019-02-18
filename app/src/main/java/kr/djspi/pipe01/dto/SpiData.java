package kr.djspi.pipe01.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SpiData {
    public abstract void setId(int id);
}
