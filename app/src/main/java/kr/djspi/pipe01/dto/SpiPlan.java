package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class SpiPlan extends SpiData {

    private int id;
    private String plan_plane;
    private String plan_section;
}
