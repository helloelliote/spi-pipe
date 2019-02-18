package kr.djspi.pipe01.dto;

import java.util.Date;
import lombok.Data;

@Data
public class SpiDate extends SpiData {

    private int id;
    private Date date;
    private Date date_rev;
}
