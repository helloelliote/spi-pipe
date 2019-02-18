package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiGeoLocation extends SpiData {

    private int id;
    private double latitude;
    private double longitude;
    private int count;
}
