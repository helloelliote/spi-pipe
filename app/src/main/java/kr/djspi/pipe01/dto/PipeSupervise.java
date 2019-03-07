package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipeSupervise implements DataItem {

    private int id;
    /**
     * 관리 기관과 관리기관 연락처
     */
    private String supervise;
    private String contact;
}
