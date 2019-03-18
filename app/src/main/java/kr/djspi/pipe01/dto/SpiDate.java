package kr.djspi.pipe01.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SpiDate implements DataItem {

    private int id = -1;
    /**
     * 최초 입력 일자(date) 와 수정 일자(date_rev): 사용자가 직접 입력하지 않는 값
     */
    private Date date;
    private Date date_rev;
}
